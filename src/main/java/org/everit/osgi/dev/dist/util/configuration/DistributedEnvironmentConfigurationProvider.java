/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.dist.util.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.everit.osgi.dev.dist.util.configuration.schema.ArgumentsType;
import org.everit.osgi.dev.dist.util.configuration.schema.EntryType;
import org.everit.osgi.dev.dist.util.configuration.schema.EnvironmentType;
import org.everit.osgi.dev.dist.util.configuration.schema.LaunchConfigOverrideType;
import org.everit.osgi.dev.dist.util.configuration.schema.LaunchConfigOverridesType;
import org.everit.osgi.dev.dist.util.configuration.schema.LaunchConfigType;
import org.everit.osgi.dev.dist.util.configuration.schema.ObjectFactory;
import org.everit.osgi.dev.dist.util.configuration.schema.UseByType;

/**
 * Provider of the configuration of an environment.
 */
public class DistributedEnvironmentConfigurationProvider {

  private static final Comparator<EntryType> ENTRY_COMPARATOR = new Comparator<EntryType>() {
    @Override
    public int compare(final EntryType o1, final EntryType o2) {
      return o1.getKey().compareTo(o2.getKey());
    }
  };

  private static final JAXBContext JAXB_CONTEXT;

  static {
    try {
      JAXB_CONTEXT = JAXBContext.newInstance(
          ObjectFactory.class.getPackage().getName(),
          ObjectFactory.class.getClassLoader());
    } catch (JAXBException e) {
      throw new RuntimeException(
          "Could not create JAXB Context for distribution configuration file", e);
    }
  }

  /**
   * Returns the {@link LaunchConfigurationDTO}.
   *
   * @param overriddenDistributionPackage
   *          the distribution package used to create the environment configuration
   * @return the environment configuration
   */
  public LaunchConfigurationDTO getLaunchConfiguration(
      final EnvironmentType overriddenDistributionPackage) {

    if (overriddenDistributionPackage == null) {
      return null;
    }

    LaunchConfigType launchConfig = overriddenDistributionPackage.getLaunchConfig();

    String mainClass = launchConfig.getMainClass();
    String classpath = launchConfig.getClassPath();

    List<String> vmArguments = new ArrayList<>();
    ArgumentsType vmArgumentsType = launchConfig.getVmArguments();
    if (vmArgumentsType != null) {
      List<EntryType> vmArgumentNodes = vmArgumentsType.getArgument();
      for (EntryType vmArgumentNode : vmArgumentNodes) {
        String vmArgumentValue = vmArgumentNode.getValue();
        vmArguments.add(vmArgumentValue);
      }
    }

    List<String> programArguments = new ArrayList<>();
    ArgumentsType programArgumentsType = launchConfig.getProgramArguments();
    if (programArgumentsType != null) {
      List<EntryType> programArgumentNodes = programArgumentsType.getArgument();
      for (EntryType programArgument : programArgumentNodes) {
        String programArgumentValue = programArgument.getValue();
        programArguments.add(programArgumentValue);
      }
    }

    return new LaunchConfigurationDTO(mainClass, classpath, vmArguments, programArguments);
  }

  /**
   * Returns the overrided distribution package read from the eosgi.dist.xml. The overrides section
   * is processed based on the given useBy argument. This means that the returned objects
   * {@link LaunchConfigType#getOverrides()} will return <code>null</code>.
   *
   * @param distConfigFile
   *          the file of the eosgi.dist.xml
   * @param useBy
   *          the type of the usage
   * @return the distribution package
   */
  public EnvironmentType getOverriddenDistributedEnvironmentConfig(
      final File distConfigFile, final UseByType useBy) {

    EnvironmentType distributionPackageType = readDistConfig(distConfigFile);

    if (distributionPackageType == null) {
      return null;
    }

    LaunchConfigType launchConfig = distributionPackageType.getLaunchConfig();

    LaunchConfigOverridesType launchConfigOverrides = launchConfig.getOverrides();
    if (launchConfigOverrides == null) {
      return distributionPackageType;
    }

    for (LaunchConfigOverrideType launchConfigOverride : launchConfigOverrides.getOverride()) {
      if (launchConfigOverride.getUseBy().equals(useBy)) {

        launchConfig.setVmArguments(
            overrideVmArguments(
                launchConfig.getVmArguments(), launchConfigOverride.getVmArguments()));

        launchConfig.setProgramArguments(
            overridePropgramArguments(
                launchConfig.getProgramArguments(), launchConfigOverride.getProgramArguments()));
      }
    }

    launchConfig.setOverrides(null);

    normalizeArguments(launchConfig.getProgramArguments());
    normalizeArguments(launchConfig.getVmArguments());

    return distributionPackageType;
  }

  private void normalizeArguments(final ArgumentsType arguments) {
    if (arguments == null) {
      return;
    }
    removeNullOrEmptyValues(arguments.getArgument());
    Collections.sort(arguments.getArgument(), ENTRY_COMPARATOR);
  }

  private List<EntryType> override(final List<EntryType> originals,
      final List<EntryType> overrides) {

    List<EntryType> rvals = new ArrayList<>(originals);

    for (EntryType override : overrides) {

      String overridingKey = override.getKey();
      String overridingValue = override.getValue();

      boolean keyShouldBeDeleted = false;
      boolean overridden = false;

      for (EntryType rval : rvals) {
        String rvalKey = rval.getKey();

        if (overridingKey.equals(rvalKey)) {

          if (overridingValue == null) {
            keyShouldBeDeleted = true;
            break;
          } else {
            rval.setValue(overridingValue);
            overridden = true;
            break;
          }

        }

      }

      if (keyShouldBeDeleted) {
        removeKey(rvals, overridingKey);
      }

      if (!overridden) {
        rvals.add(override);
      }
    }

    return rvals;
  }

  private ArgumentsType overridePropgramArguments(final ArgumentsType original,
      final ArgumentsType override) {

    List<EntryType> any = override(
        original == null ? new ArrayList<EntryType>() : original.getArgument(),
        override == null ? new ArrayList<EntryType>() : override.getArgument());

    ArgumentsType rval = new ArgumentsType();
    rval.getArgument().addAll(any);
    return rval;
  }

  private ArgumentsType overrideVmArguments(final ArgumentsType original,
      final ArgumentsType override) {

    List<EntryType> any = override(
        original == null ? new ArrayList<EntryType>() : original.getArgument(),
        override == null ? new ArrayList<EntryType>() : override.getArgument());

    ArgumentsType rval = new ArgumentsType();
    rval.getArgument().addAll(any);
    return rval;
  }

  /**
   * Returns the original distribution package read from the eosgi.dist.xml.
   */
  private EnvironmentType readDistConfig(final File distConfigFile) {
    if (!distConfigFile.exists()) {
      return null;
    }

    try (FileInputStream fin = new FileInputStream(distConfigFile)) {
      Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();

      Object distributionPackage = unmarshaller.unmarshal(fin);
      if (distributionPackage instanceof JAXBElement) {

        @SuppressWarnings("unchecked")
        JAXBElement<EnvironmentType> jaxbDistPack =
            (JAXBElement<EnvironmentType>) distributionPackage;
        distributionPackage = jaxbDistPack.getValue();
      }
      if (distributionPackage instanceof EnvironmentType) {
        return (EnvironmentType) distributionPackage;
      } else {
        throw new IllegalStateException(
            "The root element in the provided distribution configuration file "
                + "is not the expected DistributionPackage element");
      }
    } catch (

    JAXBException e) {
      throw new IllegalStateException(
          "Failed to process already existing distribution configuration file: "
              + distConfigFile.getAbsolutePath(),
          e);
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Can't read distribution configuration file: " + distConfigFile.getAbsolutePath(), e);
    }
  }

  private void removeKey(final List<EntryType> rvals, final String overridingKey) {
    Iterator<EntryType> rvalIterator = rvals.iterator();
    while (rvalIterator.hasNext()) {

      EntryType rval = rvalIterator.next();
      String rvalKey = rval.getKey();

      if (rvalKey.equals(overridingKey)) {
        rvalIterator.remove();
      }
    }
  }

  private void removeNullOrEmptyValues(final List<EntryType> rvals) {

    Iterator<EntryType> rvalIterator = rvals.iterator();
    while (rvalIterator.hasNext()) {

      EntryType rval = rvalIterator.next();
      String rvalValue = rval.getValue();

      if (rvalValue == null || "".equals(rvalValue.trim())) {
        rvalIterator.remove();
      }
    }
  }

}
