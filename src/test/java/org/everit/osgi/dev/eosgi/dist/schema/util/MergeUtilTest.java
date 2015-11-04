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

import org.junit.Assert;
import org.junit.Test;

public class MergeUtilTest {

  @Test
  public void testMergeDefaults() {

    Map<String, String> default1 = new HashMap<String, String>();
    default1.put("k1", "v1");
    default1.put("k2", "v2");
    default1.put("k3", "v3");

    Map<String, String> default2 = new HashMap<String, String>();
    default2.put("k1", "x1");
    default2.put("k2", "");
    default2.put("k4", "v4");

    Map<String, String> result = MergeUtil.mergeDefaults(default1, default2);

    Assert.assertEquals(3, result.size());
    Assert.assertEquals("x1", result.get("k1"));
    Assert.assertEquals(null, result.get("k2"));
    Assert.assertEquals("v3", result.get("k3"));
    Assert.assertEquals("v4", result.get("k4"));
  }

  @Test
  public void testMergeDefaultsWithNull() {

    Map<String, String> default0 = new HashMap<String, String>();
    default0.put("k1", "v1");

    Map<String, String> result = MergeUtil.mergeDefaults(null, default0);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("v1", result.get("k1"));

    result = MergeUtil.mergeDefaults(default0, null);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("v1", result.get("k1"));
  }

  @Test
  public void testMergeOverrides() {

    Map<String, String> override1 = new HashMap<String, String>();
    override1.put("k1", "v1");
    override1.put("k2", "v2");
    override1.put("k3", "v3");
    override1.put("k4", "v4");
    override1.put("k5", "v5");

    Map<String, String> default2 = new HashMap<String, String>();
    default2.put("k1", "x1");
    default2.put("k2", "x2");
    default2.put("k3", "");
    default2.put("k6", "v6");

    Map<String, String> override2 = new HashMap<String, String>();
    override2.put("k2", "x3");
    override2.put("k3", "x4");
    override2.put("k4", "x5");
    override2.put("k7", "v7");

    Map<String, String> result = MergeUtil.mergeOverrides(override1, default2, override2);

    Assert.assertEquals(5, result.size());
    Assert.assertEquals(null, result.get("k1"));
    Assert.assertEquals("x3", result.get("k2"));
    Assert.assertEquals("x4", result.get("k3"));
    Assert.assertEquals("x5", result.get("k4"));
    Assert.assertEquals("v5", result.get("k5"));
    Assert.assertEquals(null, result.get("k6"));
    Assert.assertEquals("v7", result.get("k7"));
  }

  @Test
  public void testMergeOverridesWithNull() {

    Map<String, String> override0 = new HashMap<String, String>();
    override0.put("k1", "v1");

    Map<String, String> result = MergeUtil.mergeOverrides(null, null, override0);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("v1", result.get("k1"));

    result = MergeUtil.mergeOverrides(null, override0, null);

    Assert.assertEquals(0, result.size());
    Assert.assertEquals(null, result.get("k1"));

    result = MergeUtil.mergeOverrides(override0, null, null);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("v1", result.get("k1"));
  }

}
