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
 * The unmodifiable arguments that can be used to execute an environment.
 */
public class EnvironmentConfigurationDTO {

  public final String classpath;

  public final String mainClass;

  public final String mainJar;

  public final List<String> programArguments;

  public final List<String> systemProperties;

  public final List<String> vmArguments;

  /**
   * Constructor.
   */
  public EnvironmentConfigurationDTO(final String mainJar, final String mainClass,
      final String classpath, final List<String> systemProperties, final List<String> vmArguments,
      final List<String> programArguments) {
    super();
    this.mainJar = mainJar;
    this.mainClass = mainClass;
    this.classpath = classpath;
    this.systemProperties = Collections.unmodifiableList(systemProperties);
    this.vmArguments = Collections.unmodifiableList(vmArguments);
    this.programArguments = Collections.unmodifiableList(programArguments);
  }

}
