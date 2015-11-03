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
package org.everit.osgi.dev.eosgi.dist.schema.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility to merge "default" and "override" maps.
 */
public final class MergeUtil {

  /**
   * Merges two default maps.
   *
   * @return The returned new map will contain the union of the two maps except the keys where the
   *         values of the second map is <code>null</code> or an empty String.
   */
  public static Map<String, String> mergeDefaults(
      final Map<String, String> default1,
      final Map<String, String> default2) {

    Map<String, String> rval = new HashMap<String, String>(default1);

    if ((default2 == null) || default2.isEmpty()) {
      return rval;
    }

    for (Entry<String, String> overrideEntry : default2.entrySet()) {

      String overrideKey = overrideEntry.getKey();
      String overrideValue = overrideEntry.getValue();

      if ((overrideValue == null) || overrideValue.isEmpty()) {
        rval.remove(overrideKey);
      } else {
        rval.put(overrideKey, overrideValue);
      }
    }

    return rval;
  }

  /**
   * Merges two override map with a default map.
   *
   * @return The returned new map will be a merge ({@link #mergeDefaults(Map, Map)}) of the third
   *         map and with a map of keys from the first map except the keys where the values of the
   *         second map is <code>null</code> or an empty String or the first map contains the key of
   *         a second map.
   */
  public static Map<String, String> mergeOverrides(
      final Map<String, String> override1,
      final Map<String, String> default2,
      final Map<String, String> override2) {

    Map<String, String> cleaned = new HashMap<String, String>(override1);

    if ((default2 != null) && !default2.isEmpty()) {
      for (Entry<String, String> overrideEntry : default2.entrySet()) {

        String overrideKey = overrideEntry.getKey();
        String overrideValue = overrideEntry.getValue();

        if ((overrideValue == null) || overrideValue.isEmpty()
            || override1.containsKey(overrideKey)) {
          cleaned.remove(overrideKey);
        }
      }
    }

    return MergeUtil.mergeDefaults(cleaned, override2);
  }

  private MergeUtil() {
  }

}
