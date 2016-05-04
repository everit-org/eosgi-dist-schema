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
package org.everit.osgi.dev.eosgi.dist.schema.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.everit.osgi.dev.eosgi.dist.schema.xsd.DistributionPackageType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.LaunchConfigOverrideType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.LaunchConfigOverridesType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.LaunchConfigType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.ObjectFactory;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.ProgramArgumentsType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.SystemPropertiesType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.UseByType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.VmArgumentsType;
import org.w3c.dom.Node;

/**
 * Provider of the configuration of an environment.
 */
public class DistributedEnvironmentConfigurationProvider {

  private final JAXBContext jaxbContext;

  /**
   * Constructor.
   */
  public DistributedEnvironmentConfigurationProvider() {
    try {
      jaxbContext = JAXBContext.newInstance(
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
      final DistributionPackageType overriddenDistributionPackage) {

    if (overriddenDistributionPackage == null) {
      return null;
    }

    LaunchConfigType launchConfig = overriddenDistributionPackage.getLaunchConfig();

    String mainClass = launchConfig.getMainClass();
    String classpath = launchConfig.getClassPath();

    List<String> systemProperties = new ArrayList<String>();
    SystemPropertiesType systemPropertiesType = launchConfig.getSystemProperties();
    if (systemPropertiesType != null) {
      List<Object> systemPropertyNodes = systemPropertiesType.getAny();
      for (Object systemPropertyNode : systemPropertyNodes) {
        Node node = (Node) systemPropertyNode;
        String systemPropertyKey = node.getNodeName();
        String systemPropertyValue = node.getTextContent();
        systemProperties.add("-D" + systemPropertyKey + "=" + systemPropertyValue);
      }
    }

    List<String> vmArguments = new ArrayList<String>();
    VmArgumentsType vmArgumentsType = launchConfig.getVmArguments();
    if (vmArgumentsType != null) {
      List<Object> vmArgumentNodes = vmArgumentsType.getAny();
      for (Object vmArgumentNode : vmArgumentNodes) {
        Node node = (Node) vmArgumentNode;
        String vmArgumentValue = node.getTextContent();
        vmArguments.add(vmArgumentValue);
      }
    }

    List<String> programArguments = new ArrayList<String>();
    ProgramArgumentsType programArgumentsType = launchConfig.getProgramArguments();
    if (programArgumentsType != null) {
      List<Object> programArgumentNodes = programArgumentsType.getAny();
      for (Object programArgumentNode : programArgumentNodes) {
        Node node = (Node) programArgumentNode;
        String programArgumentValue = node.getTextContent();
        programArguments.add(programArgumentValue);
      }
    }

    return new LaunchConfigurationDTO(
        mainClass, classpath, systemProperties, vmArguments, programArguments);
  }

  /**
   * Returns the overrided distribution package read from the eosgi.dist.xml. The overrides section
   * is processed based on the given useBy argument. This means that the returned objects
   * {@link LaunchConfigType#getOverrides()} will return <code>null</code>.
   *
   * @param distFolderFile
   *          the file of the eosgi.dist.xml
   * @param useBy
   *          the type of the usage
   * @return the distribution package
   */
  public DistributionPackageType getOverriddenDistributionPackage(final File distFolderFile,
      final UseByType useBy) {

    DistributionPackageType distributionPackageType = readDistConfig(distFolderFile);

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

        launchConfig.setSystemProperties(
            overrideSystemProperties(
                launchConfig.getSystemProperties(), launchConfigOverride.getSystemProperties()));

        launchConfig.setVmArguments(
            overrideVmArguments(
                launchConfig.getVmArguments(), launchConfigOverride.getVmArguments()));

        launchConfig.setProgramArguments(
            overridePropgramArguments(
                launchConfig.getProgramArguments(), launchConfigOverride.getProgramArguments()));
      }
    }

    launchConfig.setOverrides(null);

    removeNullOrEmptyValues(launchConfig.getProgramArguments().getAny());
    removeNullOrEmptyValues(launchConfig.getSystemProperties().getAny());
    removeNullOrEmptyValues(launchConfig.getVmArguments().getAny());

    return distributionPackageType;
  }

  private List<Object> override(final List<Object> originals, final List<Object> overrides) {

    List<Object> rvals = new ArrayList<Object>(originals);

    for (Object override : overrides) {

      Node overridingNode = (Node) override;
      String overridingKey = overridingNode.getNodeName();
      String overridingValue = overridingNode.getTextContent();

      boolean keyShouldBeDeleted = false;
      boolean overrided = false;

      for (Object rval : rvals) {

        Node rvalNode = (Node) rval;
        String rvalKey = rvalNode.getNodeName();

        if (overridingKey.equals(rvalKey)) {

          if ((overridingValue == null) || overridingValue.isEmpty()) {
            keyShouldBeDeleted = true;
            break;
          } else {
            rvalNode.setTextContent(overridingValue);
            overrided = true;
            break;
          }

        }

      }

      if (keyShouldBeDeleted) {
        removeKey(rvals, overridingKey);
      }

      if (!overrided) {
        rvals.add(overridingNode);
      }
    }

    return rvals;
  }

  private ProgramArgumentsType overridePropgramArguments(final ProgramArgumentsType original,
      final ProgramArgumentsType override) {

    List<Object> any = override(
        original == null ? new ArrayList<Object>() : original.getAny(),
        override == null ? new ArrayList<Object>() : override.getAny());

    ProgramArgumentsType rval = new ProgramArgumentsType();
    rval.getAny().addAll(any);
    return rval;
  }

  private SystemPropertiesType overrideSystemProperties(final SystemPropertiesType original,
      final SystemPropertiesType override) {

    List<Object> any = override(
        original == null ? new ArrayList<Object>() : original.getAny(),
        override == null ? new ArrayList<Object>() : override.getAny());

    SystemPropertiesType rval = new SystemPropertiesType();
    rval.getAny().addAll(any);
    return rval;
  }

  private VmArgumentsType overrideVmArguments(final VmArgumentsType original,
      final VmArgumentsType override) {

    List<Object> any = override(
        original == null ? new ArrayList<Object>() : original.getAny(),
        override == null ? new ArrayList<Object>() : override.getAny());

    VmArgumentsType rval = new VmArgumentsType();
    rval.getAny().addAll(any);
    return rval;
  }

  /**
   * Returns the original distribution package read from the eosgi.dist.xml.
   */
  private DistributionPackageType readDistConfig(final File distFolderFile) {

    File distConfigFile = new File(distFolderFile, "/.eosgi.dist.xml");
    if (!distConfigFile.exists()) {
      return null;
    }

    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      Object distributionPackage = unmarshaller.unmarshal(distConfigFile);
      if (distributionPackage instanceof JAXBElement) {

        @SuppressWarnings("unchecked")
        JAXBElement<DistributionPackageType> jaxbDistPack =
            (JAXBElement<DistributionPackageType>) distributionPackage;
        distributionPackage = jaxbDistPack.getValue();
      }
      if (distributionPackage instanceof DistributionPackageType) {
        return (DistributionPackageType) distributionPackage;
      } else {
        throw new IllegalStateException(
            "The root element in the provided distribution configuration file "
                + "is not the expected DistributionPackage element");
      }
    } catch (JAXBException e) {
      throw new IllegalStateException(
          "Failed to process already existing distribution configuration file: "
              + distConfigFile.getAbsolutePath(),
          e);
    }
  }

  private void removeKey(final List<Object> rvals, final String overridingKey) {
    Iterator<Object> rvalIterator = rvals.iterator();
    while (rvalIterator.hasNext()) {

      Object rval = rvalIterator.next();
      Node rvalNode = (Node) rval;
      String rvalKey = rvalNode.getNodeName();

      if (rvalKey.equals(overridingKey)) {
        rvalIterator.remove();
      }
    }
  }

  private void removeNullOrEmptyValues(final List<Object> rvals) {

    Iterator<Object> rvalIterator = rvals.iterator();
    while (rvalIterator.hasNext()) {

      Object rval = rvalIterator.next();
      Node rvalNode = (Node) rval;
      String rvalValue = rvalNode.getTextContent();

      if ((rvalValue == null) || rvalValue.isEmpty()) {
        rvalIterator.remove();
      }
    }
  }

}
