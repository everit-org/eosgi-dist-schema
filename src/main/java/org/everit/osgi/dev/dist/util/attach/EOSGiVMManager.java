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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.everit.osgi.dev.dist.util.DistConstants;
import org.everit.osgi.dev.dist.util.attach.internal.reflect.VirtualMachineDescriptorReflect;
import org.everit.osgi.dev.dist.util.attach.internal.reflect.VirtualMachineReflect;
import org.everit.osgi.dev.dist.util.attach.internal.reflect.VirtualMachineStaticReflect;

/**
 * Tracks virtual machines that run EOSGi environment.
 */
public class EOSGiVMManager implements Closeable {

  private static final int BUFFER_SIZE = 1024;

  private boolean closed = false;

  private final Map<String, String> environmentIdByVmId = new HashMap<>();

  private final Map<String, Set<EnvironmentRuntimeInfo>> environmentInfosByEnvironmentId =
      new HashMap<>();

  private final Map<String, String> launchIdByVmId = new HashMap<>();

  private Set<String> processedVMIds = new HashSet<>();

  private File shutdownAgentFile = null;

  private final VirtualMachineStaticReflect virtualMachineStatic;

  private final Map<String, String> vmIdByLaunchId = new HashMap<>();

  /**
   * Constructor.
   * 
   * @param attachAPIClassLoader
   *          The classloader that will be used to access attach API. This is necessary as maven
   *          replaces the classloaders with plugin-specific ones and the attach API classes are not
   *          the same types as the ones that are accessed within the eclipse plugin.
   */
  public EOSGiVMManager(final ClassLoader attachAPIClassLoader) {
    virtualMachineStatic = new VirtualMachineStaticReflect(attachAPIClassLoader);

    refresh();
  }

  @Override
  public synchronized void close() {
    if (this.shutdownAgentFile != null) {
      shutdownAgentFile.delete();
    }
    launchIdByVmId.clear();
    processedVMIds.clear();
    environmentInfosByEnvironmentId.clear();
    environmentIdByVmId.clear();
    vmIdByLaunchId.clear();
    closed = true;
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
    if (closed) {
      return Collections.emptySet();
    }
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

  public synchronized String getVirtualMachineIdByIUniqueLaunchId(final String uniqueLaunchId) {
    return vmIdByLaunchId.get(uniqueLaunchId);
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

  private void processVirtualMachine(final VirtualMachineReflect virtualMachine) {

    Properties systemProperties = virtualMachine.getSystemProperties();

    String launchUniqueId = systemProperties.getProperty(DistConstants.SYSPROP_LAUNCH_UNIQUE_ID);
    String vmId = virtualMachine.id();
    if (launchUniqueId != null) {
      vmIdByLaunchId.put(launchUniqueId, vmId);
      launchIdByVmId.put(vmId, launchUniqueId);
    }

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
    environmentRuntimeInfo.virtualMachineId = vmId;

    Set<EnvironmentRuntimeInfo> environmentInfos =
        environmentInfosByEnvironmentId.get(environmentId);
    if (environmentInfos == null) {
      environmentInfos = new HashSet<>();
      environmentInfosByEnvironmentId.put(environmentId, environmentInfos);
      environmentIdByVmId.put(vmId, environmentId);
    }
    environmentInfos.add(environmentRuntimeInfo);
  }

  /**
   * Refreshes the information of EOSGi Environment VMs.
   */
  public synchronized void refresh() {
    if (closed) {
      return;
    }
    Set<String> aliveVMIds = new HashSet<>();
    List<VirtualMachineDescriptorReflect> virtualMachines = virtualMachineStatic.list();
    for (VirtualMachineDescriptorReflect virtualMachineDescriptor : virtualMachines) {
      String vmId = virtualMachineDescriptor.id();
      aliveVMIds.add(vmId);
      if (!processedVMIds.contains(vmId)) {
        try (VirtualMachineReflect virtualMachine =
            virtualMachineStatic.attach(virtualMachineDescriptor)) {
          processVirtualMachine(virtualMachine);
        }

      }
    }
    removeDeadVms(aliveVMIds);
    processedVMIds = aliveVMIds;
  }

  private void removeDeadVms(final Set<String> aliveVMIds) {
    for (String vmId : processedVMIds) {
      if (!aliveVMIds.contains(vmId)) {
        String launchId = launchIdByVmId.remove(vmId);
        if (launchId != null) {
          vmIdByLaunchId.remove(launchId);
        }

        String environmentId = environmentIdByVmId.remove(vmId);
        if (environmentId != null) {
          environmentInfosByEnvironmentId.remove(environmentId);
        }
      }
    }
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
      final Long timeout) {
    if (closed) {
      return;
    }

    try (VirtualMachineReflect virtualMachine = virtualMachineStatic.attach(virtualMachineId)) {
      if (timeout == null) {
        virtualMachine.loadAgent(getShutdownAgentPath());
      } else {
        virtualMachine.loadAgent(getShutdownAgentPath(), "timeout=" + timeout);
      }
    }
  }
}
