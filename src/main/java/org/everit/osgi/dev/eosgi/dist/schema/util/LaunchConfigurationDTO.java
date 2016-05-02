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

import java.util.Collections;
import java.util.List;

/**
 * The unmodifiable arguments that can be used to launch an environment.
 */
public class LaunchConfigurationDTO {

  public final String classpath;

  public final String mainClass;

  public final List<String> programArguments;

  public final List<String> systemProperties;

  public final List<String> vmArguments;

  /**
   * Constructor.
   *
   * @param mainClass
   *          the fully qualified name of the main class to start
   * @param classpath
   *          the classpath required to start the environment
   * @param systemProperties
   *          the system properties used to start the environment
   * @param vmArguments
   *          the VM arguments used to start the environment
   * @param programArguments
   *          the program arguments used to start environment
   */
  public LaunchConfigurationDTO(final String mainClass,
      final String classpath, final List<String> systemProperties, final List<String> vmArguments,
      final List<String> programArguments) {
    super();
    this.mainClass = mainClass;
    this.classpath = classpath;
    this.systemProperties = Collections.unmodifiableList(systemProperties);
    this.vmArguments = Collections.unmodifiableList(vmArguments);
    this.programArguments = Collections.unmodifiableList(programArguments);
  }

  /**
   * Checks if this (the new) configuration is changed compared to the existing. The jacoco
   * configuration is ignored.
   *
   * @param existingConfig
   *          the existing configuration checked against this configrutaion
   * @return <code>true</code> if the configuration is changed, otherwise <code>false</code>
   */
  public boolean isChanged(
      final LaunchConfigurationDTO existingConfig) {

    if (existingConfig == null) {
      return false;
    }

    if (isClasspathChanged(existingConfig)
        || isMainClassChanged(existingConfig)
        || isPorgramArgumentsChanged(existingConfig)
        || isSystemPropertiesChanged(existingConfig)
        || isVmArgumentsChanged(existingConfig)) {
      return true;
    }

    return false;
  }

  private boolean isClasspathChanged(final LaunchConfigurationDTO existingConfig) {
    return !existingConfig.classpath.equals(classpath);
  }

  private boolean isMainClassChanged(final LaunchConfigurationDTO existingConfig) {
    return !existingConfig.mainClass.equals(mainClass);
  }

  private boolean isPorgramArgumentsChanged(final LaunchConfigurationDTO existingConfig) {
    return !existingConfig.programArguments.equals(programArguments);
  }

  private boolean isSystemPropertiesChanged(final LaunchConfigurationDTO existingConfig) {
    return !existingConfig.systemProperties.equals(systemProperties);
  }

  private boolean isVmArgumentsChanged(final LaunchConfigurationDTO existingConfig) {

    if (existingConfig.vmArguments.size() != vmArguments.size()) {
      return true;
    }

    for (int i = 0; i < existingConfig.vmArguments.size(); i++) {
      String existingVmArg = existingConfig.vmArguments.get(i);
      String newVmArg = vmArguments.get(i);
      if (!existingVmArg.equals(newVmArg)
          && !existingVmArg.contains("jacoco")
          && !newVmArg.contains("jacoco")) {
        return true;
      }
    }

    return false;
  }

}
