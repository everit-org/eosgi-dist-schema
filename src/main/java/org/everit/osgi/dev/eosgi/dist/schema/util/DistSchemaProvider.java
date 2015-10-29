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
import org.everit.osgi.dev.eosgi.dist.schema.xsd.EnvironmentConfigurationType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.EnvironmentOverrideType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.ObjectFactory;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.OverridesType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.UseByType;
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
   * Returns the overrided distribution package read from the eosgi.dist.xml. The overrides section
   * is processed based the given useBy argument. This means that the return objects
   * {@link EnvironmentConfigurationType#getOverrides()} will return <code>null</code>.
   */
  public DistributionPackageType geOverridedDistributionPackage(final File distFolderFile,
      final UseByType useBy) {

    DistributionPackageType distributionPackageType = readDistConfig(distFolderFile);

    EnvironmentConfigurationType environmentConfiguration =
        distributionPackageType.getEnvironmentConfiguration();

    UseByType defaultUseBy = environmentConfiguration.getUseBy();

    if (defaultUseBy.equals(useBy)) {
      environmentConfiguration.setOverrides(null);
      return distributionPackageType;
    }

    OverridesType overrides = environmentConfiguration.getOverrides();
    if (overrides == null) {
      return distributionPackageType;
    }

    for (EnvironmentOverrideType environmentOverride : overrides.getOverride()) {
      if (environmentOverride.getUseBy().equals(useBy)) {
        overrideSystemProperties(environmentConfiguration, environmentOverride);
        overrideVmOptions(environmentConfiguration, environmentOverride);
      }
    }

    environmentConfiguration.setOverrides(null);
    return distributionPackageType;
  }

  private void overrideSystemProperties(final EnvironmentConfigurationType environmentConfiguration,
      final EnvironmentOverrideType environmentOverride) {

    List<Object> overridingSystemProperties = environmentOverride.getSystemProperties().getAny();
    List<Object> originalSystemProperties = environmentConfiguration.getSystemProperties().getAny();
    List<Object> systemPropertiesToAdd = new ArrayList<Object>();

    for (Object overridingSystemProperty : overridingSystemProperties) {

      Node overridingSystemPropertyNode = (Node) overridingSystemProperty;
      String overridingSystemPropertyKey = overridingSystemPropertyNode.getNodeName();
      String overridingSystemPropertyValue = overridingSystemPropertyNode.getTextContent();

      boolean overrided = false;

      for (Object originalSystemProperty : originalSystemProperties) {

        Node originalSystemPropertyNode = (Node) originalSystemProperty;
        String originalSystemPropertyKey = originalSystemPropertyNode.getNodeName();

        if (originalSystemPropertyKey.equals(overridingSystemPropertyKey)) {
          originalSystemPropertyNode.setTextContent(overridingSystemPropertyValue);
          overrided = true;
        }
      }

      if (!overrided) {
        systemPropertiesToAdd.add(overridingSystemProperty);
      }

    }

    originalSystemProperties.addAll(systemPropertiesToAdd);
  }

  private void overrideVmOptions(final EnvironmentConfigurationType environmentConfiguration,
      final EnvironmentOverrideType environmentOverride) {

    List<String> overridingVmOptions = environmentOverride.getVmOptions().getVmOption();
    List<String> originalVmOptions = environmentConfiguration.getVmOptions().getVmOption();
    List<String> vmOptionsToAdd = new ArrayList<String>();

    for (String overridingVmOption : overridingVmOptions) {

      boolean overrided = false;

      for (String originalVmOption : originalVmOptions) {

        if (originalVmOption.equals(overridingVmOption)) {
          overrided = true;
        }
      }

      if (!overrided) {
        vmOptionsToAdd.add(overridingVmOption);
      }
    }

    originalVmOptions.addAll(vmOptionsToAdd);
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
