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
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.everit.osgi.dev.dist.util.DistConstants;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class VirtualMachineManager {

  private final Map<String, Set<EnvironmentRuntimeInfo>> environmentInfosByEnvironmentId =
      new HashMap<>();

  public VirtualMachineManager() {
    refresh();
  }

  /**
   * Returns the available runtime informations for an environment.
   *
   * @param environmentId
   *          The id of the environment.
   * @param environmentRootDir
   *          The root dir of the environment.
   * @return A set of runtime informations or an empty set if no running JVM is available.
   */
  public synchronized Set<EnvironmentRuntimeInfo> getRuntimeInformations(final String environmentId,
      final File environmentRootDir) {
    Set<EnvironmentRuntimeInfo> result = new HashSet<>();
    Set<EnvironmentRuntimeInfo> environmentInfos =
        environmentInfosByEnvironmentId.get(environmentId);
    if (environmentInfos == null) {
      return result;
    }

    for (EnvironmentRuntimeInfo environmentRuntimeInfo : environmentInfos) {
      if (isParentOrSameDir(environmentRootDir, environmentRuntimeInfo.userDir)) {
        result.add(environmentRuntimeInfo);
      }
    }
    return result;
  }

  private boolean isParentOrSameDir(final File environmentRootDir, final File userDir) {
    File currentDir = userDir;
    while (currentDir != null) {
      if (currentDir.equals(environmentRootDir)) {
        return true;
      }
      currentDir = currentDir.getParentFile();
    }
    return false;
  }

  private void loadMangementAgent(final VirtualMachine virtualMachine,
      final Properties systemProperties)
      throws AgentLoadException, AgentInitializationException, IOException {
    String javaHome = systemProperties.getProperty("java.home");
    String agent = javaHome + File.separator + "lib" + File.separator + "management-agent.jar";
    virtualMachine.loadAgent(agent);
  }

  private void processVirtualMachine(final VirtualMachine virtualMachine)
      throws IOException, AgentLoadException, AgentInitializationException {

    Properties systemProperties = virtualMachine.getSystemProperties();

    String environmentId = systemProperties.getProperty(DistConstants.SYSPROP_ENVIRONMENT_ID);
    if (environmentId == null) {
      return;
    }

    String jmxURL =
        readAgentProperty(virtualMachine, "com.sun.management.jmxremote.localConnectorAddress");

    if (jmxURL == null) {
      loadMangementAgent(virtualMachine, systemProperties);
      jmxURL =
          readAgentProperty(virtualMachine, "com.sun.management.jmxremote.localConnectorAddress");
    }

    if (jmxURL == null) {
      return;
    }

    String userDir = String.valueOf(systemProperties.get("user.dir"));

    EnvironmentRuntimeInfo environmentRuntimeInfo = new EnvironmentRuntimeInfo();
    environmentRuntimeInfo.jmxServiceURL = jmxURL;
    environmentRuntimeInfo.userDir = new File(userDir);
    environmentRuntimeInfo.virtualMachineId = virtualMachine.id();

    Set<EnvironmentRuntimeInfo> environmentInfos =
        environmentInfosByEnvironmentId.get(environmentId);
    if (environmentInfos == null) {
      environmentInfos = new HashSet<>();
      environmentInfosByEnvironmentId.put(environmentId, environmentInfos);
    }
    environmentInfos.add(environmentRuntimeInfo);
  }

  private String readAgentProperty(final VirtualMachine virtualMachine, final String propertyName)
      throws IOException {
    String propertyValue = null;
    Properties agentProperties = virtualMachine.getAgentProperties();
    propertyValue = agentProperties.getProperty(propertyName);
    return propertyValue;
  }

  public synchronized void refresh() {
    environmentInfosByEnvironmentId.clear();
    List<VirtualMachineDescriptor> virtualMachines = VirtualMachine.list();
    for (VirtualMachineDescriptor virtualMachineDescriptor : virtualMachines) {
      try {
        VirtualMachine virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
        try {
          processVirtualMachine(virtualMachine);
        } finally {
          virtualMachine.detach();
        }

      } catch (AttachNotSupportedException | IOException | AgentInitializationException
          | AgentLoadException e) {
        throw new RuntimeException(
            "Error during communicating to JVM to check if it is an OSGi environment: "
                + virtualMachineDescriptor.id() + " - " + virtualMachineDescriptor.displayName(),
            e);
      }
    }
  }
}
