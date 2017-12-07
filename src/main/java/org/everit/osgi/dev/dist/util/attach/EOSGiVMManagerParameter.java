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

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Execution parameters of EOSGVMManager.
 */
public class EOSGiVMManagerParameter {

  /**
   * The classloader that will be used to access attach API. This is necessary as maven replaces the
   * classloaders with plugin-specific ones and the attach API classes are not the same types as the
   * ones that are accessed within the eclipse plugin.
   */
  public ClassLoader classLoader;

  /**
   * A consumer that accepts messages when there is a deadlock on VM call. Normally this is logged
   * on WARN level or an exception is thrown.
   */
  public Consumer<EOSGiVMManagerEventData> deadlockEventHandler;

  /**
   * Consumer that is called if an exception is thrown during attaching a VirtualMachine.
   */
  public Function<EOSGiVMManagerEventData, Boolean> exceptionDuringAttachVMHandler;

}
