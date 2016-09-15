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

public class VirtualMachineDescriptorReflect {

  private final Object wrapped;

  public VirtualMachineDescriptorReflect(final Object wrapped) {
    this.wrapped = wrapped;
  }

  public String displayName() {
    return ReflectUtil.callMethod(wrapped, "displayName", new Class<?>[0], new Object[0]);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof VirtualMachineDescriptorReflect) {
      VirtualMachineDescriptorReflect vmdr = (VirtualMachineDescriptorReflect) obj;
      return wrapped.equals(vmdr.wrapped);
    }
    return false;
  }

  public Object getWrapped() {
    return wrapped;
  }

  @Override
  public int hashCode() {
    return wrapped.hashCode();
  }

  public String id() {
    return ReflectUtil.callMethod(wrapped, "id", new Class<?>[0], new Object[0]);
  }

}
