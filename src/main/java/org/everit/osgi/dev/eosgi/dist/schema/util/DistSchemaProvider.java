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
public class DistSchemaProvider {

  private final JAXBContext jaxbContext;

  /**
   * Constructor.
   */
  public DistSchemaProvider() {
    try {
      jaxbContext =
          JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
              ObjectFactory.class.getClassLoader());
    } catch (JAXBException e) {
      throw new RuntimeException(
          "Could not create JAXB Context for distribution configuration file", e);
    }
  }

  /**
   * Applies the ovverrides on a LaunchConfig. The overrides section is processed based on the useBy
   * argument. This means that the invocation of {@link LaunchConfigType#getOverrides()} will return
   * <code>null</code>.
   */
  public void applyOverride(
      final LaunchConfigType launchConfig,
      final UseByType useBy) {

    LaunchConfigOverridesType launchConfigOverrides = launchConfig.getOverrides();
    if (launchConfigOverrides == null) {
      return;
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
  }

  /**
   * Returns the overrided distribution package read from the eosgi.dist.xml. The overrides section
   * is processed based the given useBy argument. This means that the returned objects
   * {@link LaunchConfigType#getOverrides()} will return <code>null</code>.
   */
  public DistributionPackageType geOverridedDistributionPackage(final File distFolderFile,
      final UseByType useBy) {

    DistributionPackageType distributionPackageType = readDistConfig(distFolderFile);

    applyOverride(distributionPackageType.getEnvironmentConfiguration().getLaunchConfig(), useBy);

    return distributionPackageType;
  }

  public EnvironmentConfigurationDTO getEnvironmentConfiguration(final File distFolderFile,
      final UseByType useBy) {
    return null; // TODO implement
  }

  private void override(final List<Object> originals, final List<Object> overrides) {

    List<Object> news = new ArrayList<Object>();

    for (Object override : overrides) {

      Node overridingNode = (Node) override;
      String overridingKey = overridingNode.getNodeName();
      String overridingValue = overridingNode.getTextContent();

      boolean overrided = false;

      for (Object original : originals) {

        Node originalNode = (Node) original;
        String originalKey = originalNode.getNodeName();

        if (originalKey.equals(overridingKey)) {
          originalNode.setTextContent(overridingValue);
          overrided = true;
        }
      }

      if (!overrided) {
        news.add(override);
      }

    }

    originals.addAll(news);
  }

  private ProgramArgumentsType overridePropgramArguments(final ProgramArgumentsType original,
      final ProgramArgumentsType override) {

    ProgramArgumentsType rval = original;
    if (rval == null) {
      rval = new ProgramArgumentsType();
    }
    if (override == null) {
      return rval;
    }

    override(rval.getAny(), override.getAny());

    return rval;
  }

  private SystemPropertiesType overrideSystemProperties(final SystemPropertiesType original,
      final SystemPropertiesType override) {

    SystemPropertiesType rval = original;
    if (rval == null) {
      rval = new SystemPropertiesType();
    }
    if (override == null) {
      return rval;
    }

    override(rval.getAny(), override.getAny());

    return rval;
  }

  private VmArgumentsType overrideVmArguments(final VmArgumentsType original,
      final VmArgumentsType override) {

    VmArgumentsType rval = original;
    if (rval == null) {
      rval = new VmArgumentsType();
    }
    if (override == null) {
      return rval;
    }

    override(rval.getAny(), override.getAny());

    return rval;
  }

  /**
   * Returns the original distribution package read from the eosgi.dist.xml.
   */
  public DistributionPackageType readDistConfig(final File distFolderFile) {
    File distConfigFile = new File(distFolderFile, "/.eosgi.dist.xml");
    if (distConfigFile.exists()) {
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
    } else {
      return null;
    }
  }

}
