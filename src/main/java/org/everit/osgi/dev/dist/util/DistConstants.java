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
package org.everit.osgi.dev.dist.util;

/**
 * Constants the a programmer must know about a distribution package.
 */
public final class DistConstants {

  public static final String DEFAULT_ENVIRONMENT_FRAMEWORK = "equinox";

  public static final String DEFAULT_ENVIRONMENT_ID = "equinox";

  public static final int DEFAULT_SHUTDOWN_TIMEOUT = 30000;

  public static final String FILE_NAME_EOSGI_DIST_CONFIG = ".eosgi.dist.xml";

  /**
   * Key that can be used to pass a classloader to the dist plugin that can load the Sun Attach API
   * via the data map of the execution request.
   */
  public static final String MAVEN_EXECUTION_REQUEST_DATA_KEY_ATTACH_API_CLASSLOADER =
      "org.everit.osgi.dev.attachAPIClassLoader";

  public static final String PLUGIN_PROPERTY_DIST_ONLY = "eosgi.distOnly";

  public static final String PLUGIN_PROPERTY_ENVIRONMENT_ID = "eosgi.environmentId";

  /**
   * Additional system property that is applied to each environment automatically.
   */
  public static final String SYSPROP_ENVIRONMENT_ID = "org.everit.osgi.dev.environmentId";

  public static final String SYSPROP_LAUNCH_UNIQUE_ID = "org.everit.osgi.dev.launcUniqueId";

  private DistConstants() {
  }
}
