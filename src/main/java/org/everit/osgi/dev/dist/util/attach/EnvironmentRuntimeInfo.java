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

package org.everit.osgi.dev.dist.util.attach;

import java.io.File;
import java.util.Properties;

import javax.annotation.Generated;

/**
 * Information of a running JVM.
 */
public class EnvironmentRuntimeInfo {

  public String jmxServiceURL;

  public Properties systemProperties;

  public File userDir;

  public String virtualMachineId;

  @Override
  @Generated("eclipse")
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EnvironmentRuntimeInfo other = (EnvironmentRuntimeInfo) obj;
    if (virtualMachineId == null) {
      if (other.virtualMachineId != null)
        return false;
    } else if (!virtualMachineId.equals(other.virtualMachineId))
      return false;
    return true;
  }

  @Override
  @Generated("eclipse")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((virtualMachineId == null) ? 0 : virtualMachineId.hashCode());
    return result;
  }

}
