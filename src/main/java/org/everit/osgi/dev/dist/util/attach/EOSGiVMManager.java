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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
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

/**
 * Tracks virtual machines that run EOSGi environment.
 */
public class EOSGiVMManager implements Closeable {

  private static final int BUFFER_SIZE = 1024;

  private final Map<String, Set<EnvironmentRuntimeInfo>> environmentInfosByEnvironmentId =
      new HashMap<>();

  private Set<String> processedVMIds = new HashSet<>();

  private File shutdownAgentFile = null;

  public EOSGiVMManager() {
    refresh();
  }

  @Override
  public synchronized void close() {
    if (this.shutdownAgentFile != null) {
      shutdownAgentFile.delete();
    }
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

  private String getShutdownAgentPath() {
    if (shutdownAgentFile != null) {
      return shutdownAgentFile.getAbsolutePath();
    }

    try {
      this.shutdownAgentFile = File.createTempFile("eosgi-shutdownJavaAgent", null);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    try (InputStream is =
        EOSGiVMManager.class.getResourceAsStream("/org.everit.jdk.javaagent.shutdown-1.0.0.jar");
        OutputStream out = new FileOutputStream(this.shutdownAgentFile)) {

      byte[] buffer = new byte[BUFFER_SIZE];
      int r = is.read(buffer);
      while (r >= 0) {
        out.write(buffer, 0, r);
        r = is.read(buffer);
      }
      return this.shutdownAgentFile.getAbsolutePath();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
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

  private void processVirtualMachine(final VirtualMachine virtualMachine)
      throws IOException, AgentLoadException, AgentInitializationException {

    Properties systemProperties = virtualMachine.getSystemProperties();

    String environmentId = systemProperties.getProperty(DistConstants.SYSPROP_ENVIRONMENT_ID);
    if (environmentId == null) {
      return;
    }

    String jmxURL = virtualMachine.startLocalManagementAgent();

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

  /**
   * Refreshes the information of EOSGi Environment VMs.
   */
  public synchronized void refresh() {
    environmentInfosByEnvironmentId.clear();
    Set<String> aliveVMIds = new HashSet<>();
    List<VirtualMachineDescriptor> virtualMachines = VirtualMachine.list();
    for (VirtualMachineDescriptor virtualMachineDescriptor : virtualMachines) {
      String vmId = virtualMachineDescriptor.id();
      aliveVMIds.add(vmId);
      if (!processedVMIds.contains(vmId)) {
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
                  + vmId + " - " + virtualMachineDescriptor.displayName(),
              e);
        }
      }
    }
    processedVMIds = aliveVMIds;
  }

  /**
   * Shuts down a Java {@link VirtualMachine}.
   *
   * @param virtualMachineId
   *          The id of the virtual machine.
   * @param timeout
   *          The timeout after the virtual machine is shut down forcibly.
   */
  public synchronized void shutDownVirtualMachine(final String virtualMachineId,
      final long timeout) {
    try {
      VirtualMachine virtualMachine = VirtualMachine.attach(virtualMachineId);
      virtualMachine.loadAgent(getShutdownAgentPath(), "timeout=" + timeout);
    } catch (AttachNotSupportedException | IOException | AgentLoadException
        | AgentInitializationException e) {

      throw new RuntimeException("Cannot shut down vm: " + virtualMachineId, e);
    }
  }
}
