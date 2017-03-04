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
package org.everit.osgi.dev.dist.util.attach.internal.reflect;

import java.io.Closeable;
import java.util.Properties;

/**
 * Reflection based access of the methods of a VirtualMachine instance.
 */
public class VirtualMachineReflect implements Closeable {

  private final Object wrapped;

  public VirtualMachineReflect(final Object wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public void close() {
    ReflectUtil.callMethod(wrapped, "detach", new Class<?>[0]);
  }

  public Properties getSystemProperties() {
    return ReflectUtil.callMethod(wrapped, "getSystemProperties", new Class<?>[0]);
  }

  public String id() {
    return ReflectUtil.callMethod(wrapped, "id", new Class<?>[0]);
  }

  public void loadAgent(final String agent) {
    ReflectUtil.callMethod(wrapped, "loadAgent", new Class<?>[] { String.class }, agent);
  }

  public void loadAgent(final String agent, final String options) {
    ReflectUtil.callMethod(wrapped, "loadAgent", new Class<?>[] { String.class, String.class },
        agent, options);
  }

  public String startLocalManagementAgent() {
    return ReflectUtil.callMethod(wrapped, "startLocalManagementAgent", new Class<?>[0]);
  }
}
