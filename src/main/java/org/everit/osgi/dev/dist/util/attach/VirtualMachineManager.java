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

import java.util.List;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class VirtualMachineManager {

  /**
   * Lists all of the virtual machines on the localhost.
   */
  public void listVirtualMachines() {
    List<VirtualMachineDescriptor> virtualMachines = VirtualMachine.list();
    for (VirtualMachineDescriptor virtualMachineDescriptor : virtualMachines) {
      try {
        VirtualMachine virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
        System.out.println(virtualMachineDescriptor.displayName());
        virtualMachine.detach();

      } catch (Exception e) {
        e.printStackTrace();
        // TODO
      }
    }
  }
}
