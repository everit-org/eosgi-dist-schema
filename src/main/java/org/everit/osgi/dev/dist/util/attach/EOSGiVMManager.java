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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.everit.osgi.dev.dist.util.DistConstants;
import org.everit.osgi.dev.dist.util.attach.internal.reflect.VirtualMachineDescriptorReflect;
import org.everit.osgi.dev.dist.util.attach.internal.reflect.VirtualMachineReflect;
import org.everit.osgi.dev.dist.util.attach.internal.reflect.VirtualMachineStaticReflect;

/**
 * Tracks virtual machines that run EOSGi environment.
 */
public class EOSGiVMManager implements Closeable {

  private static final int BUFFER_SIZE = 1024;

  private static final long DEFAULT_VM_CALL_TIMEOUT = 3000;

  private boolean closed = false;

  private final Consumer<String> deadlockMessageConsumer;

  private final Map<String, String> environmentIdByVmId = new HashMap<>();

  private final Map<String, Set<EnvironmentRuntimeInfo>> environmentInfosByEnvironmentId =
      new HashMap<>();

  private final Map<String, String> launchIdByVmId = new HashMap<>();

  private Set<String> processedVMIds = new HashSet<>();

  private File shutdownAgentFile = null;

  private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

  private final List<Runnable> stateChangeListeners = new ArrayList<>();

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
    this(attachAPIClassLoader, (message) -> {
      throw new RuntimeException(message);
    });
  }

  /**
   * Constructor.
   * 
   * @param attachAPIClassLoader
   *          The classloader that will be used to access attach API. This is necessary as maven
   *          replaces the classloaders with plugin-specific ones and the attach API classes are not
   *          the same types as the ones that are accessed within the eclipse plugin.
   * @param deadlockMessageConsumer
   *          A consumer that accepts messages when there is a deadlock on VM call. Normally this is
   *          logged on WARN level or an exception is thrown.
   */
  public EOSGiVMManager(final ClassLoader attachAPIClassLoader,
      final Consumer<String> deadlockMessageConsumer) {
    this.deadlockMessageConsumer = deadlockMessageConsumer;
    virtualMachineStatic = new VirtualMachineStaticReflect(attachAPIClassLoader);

    refresh();
  }

  public synchronized void addStateChangeListener(final Runnable listener) {
    stateChangeListeners.add(listener);
  }

  @SuppressWarnings("deprecation")
  private <R> R callWithTimeout(final Supplier<R> supplier, final String vmId) {
    AtomicReference<Thread> executorThread = new AtomicReference<>();
    Future<R> future = singleThreadExecutor.submit(() -> {
      executorThread.set(Thread.currentThread());
      return supplier.get();
    });

    try {
      return future.get(DEFAULT_VM_CALL_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (TimeoutException e) {
      Thread thread = executorThread.get();
      if (thread.isAlive()) {
        thread.stop();
      }
      deadlockMessageConsumer.accept("Could not execute command on VM. This happens sometimes"
          + " on Windows systems when the VM stops at the same time as the command is called: "
          + vmId);
      return null;
    }
  }

  @Override
  public synchronized void close() {
    singleThreadExecutor.shutdown();
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
    String vmId = virtualMachine.id();
    Properties systemProperties = callWithTimeout(() -> virtualMachine.getSystemProperties(), vmId);

    if (systemProperties == null) {
      return;
    }

    String launchUniqueId = systemProperties.getProperty(DistConstants.SYSPROP_LAUNCH_UNIQUE_ID);

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
    }
    environmentInfos.add(environmentRuntimeInfo);
    environmentIdByVmId.put(vmId, environmentId);
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

    if (!processedVMIds.equals(aliveVMIds)) {
      for (Runnable listener : stateChangeListeners) {
        listener.run();
      }
    }
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

          Set<EnvironmentRuntimeInfo> runtimeInfos =
              environmentInfosByEnvironmentId.get(environmentId);

          Iterator<EnvironmentRuntimeInfo> iterator = runtimeInfos.iterator();
          while (iterator.hasNext()) {
            EnvironmentRuntimeInfo environmentRuntimeInfo = iterator.next();
            if (environmentRuntimeInfo.virtualMachineId.equals(vmId)) {
              iterator.remove();
            }
          }
          if (runtimeInfos.isEmpty()) {
            environmentInfosByEnvironmentId.remove(environmentId);
          }
        }
      }
    }
  }

  public synchronized void removeStateChangeListener(final Runnable listener) {
    stateChangeListeners.remove(listener);
  }

  /**
   * Shuts down a Java {@link VirtualMachine}.
   *
   * @param virtualMachineId
   *          The id of the virtual machine.
   * @param exitcode
   *          The exit code that the application should return after a normal shutdown.
   * @param forcedShutdownParameter
   *          Parameter that tells why and how forced shutdown should be applied or
   *          <code>null</code> if no forced shutdown should be done.
   */
  public synchronized void shutDownVirtualMachine(final String virtualMachineId, final int exitcode,
      final ForcedShutdownParameter forcedShutdownParameter) {
    if (closed) {
      return;
    }

    try (VirtualMachineReflect virtualMachine = virtualMachineStatic.attach(virtualMachineId)) {
      callWithTimeout(() -> {
        if (forcedShutdownParameter == null) {
          virtualMachine.loadAgent(getShutdownAgentPath());
        } else {
          virtualMachine.loadAgent(getShutdownAgentPath(), "timeout="
              + forcedShutdownParameter.timeout + ",haltcode=" + forcedShutdownParameter.haltCode);
        }
        return null;
      }, virtualMachine.id());

    }
  }
}
