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

import java.util.List;

/**
 * Arguments that can be used to launch an environment.
 */
public class LaunchConfigurationDTO {

  public String classpath;

  public String mainClass;

  public List<String> programArguments;

  public List<String> vmArguments;

  /**
   * Constructor.
   *
   * @param mainClass
   *          the fully qualified name of the main class to start
   * @param classpath
   *          the classpath required to start the environment
   * @param vmArguments
   *          the VM arguments used to start the environment
   * @param programArguments
   *          the program arguments used to start environment
   */
  public LaunchConfigurationDTO(final String mainClass,
      final String classpath, final List<String> vmArguments,
      final List<String> programArguments) {
    super();
    this.mainClass = mainClass;
    this.classpath = classpath;
    this.vmArguments = vmArguments;
    this.programArguments = programArguments;
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

  private boolean isVmArgumentsChanged(final LaunchConfigurationDTO existingConfig) {
    return !existingConfig.vmArguments.equals(vmArguments);
  }

}
