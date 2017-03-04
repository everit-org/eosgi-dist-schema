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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Class that allows accessing the static functions of VirtualMachine class via reflection.
 */
public class VirtualMachineStaticReflect {

  private Class<?> virtualMachineClass;

  private Class<?> virtualMachineDescriptorClass;

  /**
   * Constructor.
   *
   * @param attachAPIClassLoader
   *          The classloader that loads the native attach API.
   */
  public VirtualMachineStaticReflect(final ClassLoader attachAPIClassLoader) {
    try {
      virtualMachineClass = attachAPIClassLoader.loadClass(VirtualMachine.class.getName());

      virtualMachineDescriptorClass =
          attachAPIClassLoader.loadClass(VirtualMachineDescriptor.class.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Attaches a VirtualMachine and gives back its proxy instance.
   *
   * @param virtualMachineId
   *          The id of the virtual machine.
   * @return The reflection based VirtualMachine instance.
   */
  public VirtualMachineReflect attach(final String virtualMachineId) {

    Object virtualMachineObj = ReflectUtil.callMethod(virtualMachineClass, "attach",
        new Class<?>[] { String.class }, new Object[] { virtualMachineId });

    return new VirtualMachineReflect(virtualMachineObj);
  }

  /**
   * Attaches a JVM.
   *
   * @param virtualMachineDescriptorReflect
   *          The descriptor of the JVM.
   * @return The virtual machine embedded in the way that the classloader that loaded the Attach
   *         native API will be used.
   */
  public VirtualMachineReflect attach(
      final VirtualMachineDescriptorReflect virtualMachineDescriptorReflect) {

    Object virtualMachineObj = ReflectUtil.callMethod(virtualMachineClass, "attach",
        new Class<?>[] { virtualMachineDescriptorClass },
        new Object[] { virtualMachineDescriptorReflect.getWrapped() });

    return new VirtualMachineReflect(virtualMachineObj);
  }

  /**
   * Lists the available JVMs.
   *
   * @return the virtual machine descriptors.
   */
  public List<VirtualMachineDescriptorReflect> list() {
    try {
      Method method = virtualMachineClass.getMethod("list");
      Object virtualMachineListObject = method.invoke(null);
      List<?> vmdList =
          ReflectUtil.createProxy(List.class, virtualMachineListObject);

      List<VirtualMachineDescriptorReflect> result = new ArrayList<>();
      for (Object vmdObj : vmdList) {
        result.add(new VirtualMachineDescriptorReflect(vmdObj));
      }
      return result;
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

}
