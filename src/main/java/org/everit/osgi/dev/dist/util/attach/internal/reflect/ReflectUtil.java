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
import java.lang.reflect.Proxy;

/**
 * Helper methods to do reflection based calls.
 */
public final class ReflectUtil {

  /**
   * Calls a method on an object with the specific method name and parameter types.
   *
   * @param obj
   *          The object instance.
   * @param methodName
   *          The name of the method.
   * @param parameterTypes
   *          The type of the parameters of the method.
   * @param args
   *          The parameter instances.
   * @return The return value of the method.
   */
  public static <R> R callMethod(final Object obj, final String methodName,
      final Class<?>[] parameterTypes, final Object... args) {

    Class<? extends Object> clazz = (obj instanceof Class<?>) ? (Class<?>) obj : obj.getClass();
    try {
      Method method = clazz.getMethod(methodName, parameterTypes);
      @SuppressWarnings("unchecked")
      R result = (R) method.invoke(obj, args);
      return result;
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Creates a proxy instance that passes each method calls to the target object. By using a proxy
   * the object can be loaded with one classLoader and the interface with another.
   *
   * @param interfaze
   *          The interface that the proxy instance implements.
   * @param object
   *          The object that implements the methods specified in the interface.
   * @return The proxy instance.
   */
  public static <T> T createProxy(final Class<T> interfaze, final Object object) {
    @SuppressWarnings("unchecked")
    T result = (T) Proxy.newProxyInstance(interfaze.getClassLoader(), new Class[] { interfaze },
        (proxy, method, args) -> {
          Method objectMethod =
              object.getClass().getMethod(method.getName(), method.getParameterTypes());
          return objectMethod.invoke(object, args);
        });
    return result;
  }

  private ReflectUtil() {
  }

}
