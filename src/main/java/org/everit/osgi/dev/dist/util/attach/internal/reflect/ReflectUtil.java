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

public final class ReflectUtil {

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
