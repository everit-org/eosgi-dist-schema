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
package org.everit.osgi.dev.dist.util.configuration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.everit.osgi.dev.dist.util.configuration.schema.ArgumentsType;
import org.everit.osgi.dev.dist.util.configuration.schema.ArtifactsType;
import org.everit.osgi.dev.dist.util.configuration.schema.EntryType;
import org.everit.osgi.dev.dist.util.configuration.schema.EnvironmentType;
import org.everit.osgi.dev.dist.util.configuration.schema.LaunchConfigType;
import org.everit.osgi.dev.dist.util.configuration.schema.ParsablesType;
import org.everit.osgi.dev.dist.util.configuration.schema.UseByType;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

public class DistSchemaProviderTest {

  private final DistributedEnvironmentConfigurationProvider distSchemaProvider =
      new DistributedEnvironmentConfigurationProvider();

  private void assertArtifacts(final ArtifactsType artifacts1, final ArtifactsType artifacts2,
      final ArtifactsType artifacts3) {

    Assert.assertNotNull(artifacts1);
    Assert.assertNotNull(artifacts2);
    Assert.assertNotNull(artifacts3);

    ReflectionAssert.assertReflectionEquals(artifacts1.getArtifact(), artifacts2.getArtifact());
    ReflectionAssert.assertReflectionEquals(artifacts2.getArtifact(), artifacts3.getArtifact());
  }

  private void assertEquals(final Object expected,
      final Object actual1, final Object actual2, final Object actual3) {
    Assert.assertEquals(expected, actual1);
    Assert.assertEquals(actual1, actual2);
    Assert.assertEquals(actual2, actual3);
  }

  private void assertMap(final Map<String, String> actualMap,
      final String... expctedKeyValuePairs) {

    Map<String, String> expectedMap = convertKeyValuesPairsToMap(expctedKeyValuePairs);
    assertMapEquals(expectedMap, actualMap);
  }

  private void assertMapEquals(final Map<String, String> expected,
      final Map<String, String> actual) {

    Assert.assertNotNull(actual);

    Set<String> processedKeys = new HashSet<String>();

    for (Entry<String, String> e : expected.entrySet()) {

      String expectedKey = e.getKey();
      String expectedValue = e.getValue();
      String actualValue = actual.get(expectedKey);

      if (((expectedValue == null) && (actualValue != null))
          || ((expectedValue != null) && !expectedValue.equals(actualValue))) {
        Assert.fail("key value pair does not match: " + "expected key [" + expectedKey
            + "] value [" + expectedValue + "] actual value [" + actualValue + "]\n"
            + "expected map [" + expected + "]\n"
            + "actual map [" + actual + "]");
      }

      processedKeys.add(expectedKey);
    }

    Map<String, String> actualClone = new HashMap<String, String>(actual);
    for (String processedKey : processedKeys) {
      actualClone.remove(processedKey);
    }

    Assert.assertTrue("More key value pairs than expected: " + actualClone + "\n"
        + "expected map [" + expected + "]\n"
        + "actual map [" + actual + "]",
        actualClone.isEmpty());
  }

  private void assertParsables(final ParsablesType parsables1, final ParsablesType parsables2,
      final ParsablesType parsables3) {

    Assert.assertNotNull(parsables1);
    Assert.assertNotNull(parsables2);
    Assert.assertNotNull(parsables3);

    ReflectionAssert.assertReflectionEquals(parsables1, parsables2);
    ReflectionAssert.assertReflectionEquals(parsables2, parsables3);
  }

  private void assertSameInformation(
      final EnvironmentType distIde,
      final EnvironmentType distTest,
      final EnvironmentType distParsables) {

    assertEquals("equinoxtest",
        distIde.getId(),
        distTest.getId(),
        distParsables.getId());
    assertEquals(null,
        distIde.getFrameworkStartLevel(),
        distTest.getFrameworkStartLevel(),
        distParsables.getFrameworkStartLevel());
    assertEquals(null,
        distIde.getInitialBundleStartLevel(),
        distTest.getInitialBundleStartLevel(),
        distParsables.getInitialBundleStartLevel());

    assertArtifacts(
        distIde.getArtifacts(),
        distTest.getArtifacts(),
        distParsables.getArtifacts());

    assertParsables(
        distIde.getParsables(),
        distTest.getParsables(),
        distParsables.getParsables());

    assertEquals(distIde.getLaunchConfig().getClassPath(),
        distIde.getLaunchConfig().getClassPath(),
        distTest.getLaunchConfig().getClassPath(),
        distParsables.getLaunchConfig().getClassPath());

    assertEquals(distIde.getLaunchConfig().getMainClass(),
        distIde.getLaunchConfig().getMainClass(),
        distTest.getLaunchConfig().getMainClass(),
        distParsables.getLaunchConfig().getMainClass());

  }

  private void assertSameInformation(final LaunchConfigurationDTO ideConf,
      final LaunchConfigurationDTO testConf, final LaunchConfigurationDTO parsablesConf) {

    assertEquals(ideConf.classpath,
        ideConf.classpath,
        testConf.classpath,
        parsablesConf.classpath);

    assertEquals(ideConf.mainClass,
        ideConf.mainClass,
        testConf.mainClass,
        parsablesConf.mainClass);

  }

  private Map<String, String> convertKeyValuesPairsToMap(final String... keyValuePairs) {

    if ((keyValuePairs.length % 2) != 0) {
      Assert.fail("the last key defined without value");
    }

    Map<String, String> rval = new HashMap<String, String>();

    for (int i = 0; i < keyValuePairs.length; i = i + 2) {
      rval.put(keyValuePairs[i], keyValuePairs[i + 1]);
    }

    return rval;
  }

  private File getDistFolderFile() throws URISyntaxException {
    URL distsResourceURL = getClass().getResource("/dists");
    Path distsPath = Paths.get(distsResourceURL.toURI());
    File distFolderFile = distsPath.toFile();
    return distFolderFile;
  }

  @Test
  public void testGetEnvironmentConfiguration() throws URISyntaxException {

    File distFolderFile = getDistFolderFile();

    EnvironmentType ideDistributionPackage =
        distSchemaProvider.getOverriddenDistributedEnvironmentConfig(distFolderFile, UseByType.IDE);

    LaunchConfigurationDTO ideConf = distSchemaProvider
        .getLaunchConfiguration(ideDistributionPackage);
    Assert.assertNotNull(ideConf);

    EnvironmentType integrationTestDistributionPackage =
        distSchemaProvider.getOverriddenDistributedEnvironmentConfig(distFolderFile,
            UseByType.INTEGRATION_TEST);

    LaunchConfigurationDTO testConf = distSchemaProvider
        .getLaunchConfiguration(integrationTestDistributionPackage);
    Assert.assertNotNull(testConf);

    EnvironmentType parsablesDistributionPackage =
        distSchemaProvider.getOverriddenDistributedEnvironmentConfig(distFolderFile,
            UseByType.PARSABLES);

    LaunchConfigurationDTO parsablesConf = distSchemaProvider
        .getLaunchConfiguration(parsablesDistributionPackage);
    Assert.assertNotNull(parsablesConf);

    assertSameInformation(ideConf, testConf, parsablesConf);

    Assert.assertArrayEquals(
        new String[] {
            "-javaagent:org.jacoco.agent-0.7.5.201505241946-runtime.jar="
                + "append=true,dumponexit=true,"
                + "output=tcpserver,"
                + "destfile=equinoxtest\\jacoco.exec,"
                + "sessionid=equinoxtest_1447147594596" },
        ideConf.vmArguments.toArray(new String[] {}));
    Assert.assertArrayEquals(
        new String[] {},
        ideConf.programArguments.toArray(new String[] {}));

    Assert.assertArrayEquals(
        new String[] {
            "-javaagent:org.jacoco.agent-0.7.5.201505241946-runtime.jar="
                + "append=true,dumponexit=true,"
                + "includes=org.everit.osgi.resource.*,"
                + "output=file,destfile=equinoxtest\\jacoco.exec,"
                + "sessionid=equinoxtest_1447147594596" },
        testConf.vmArguments.toArray(new String[] {}));
    Assert.assertArrayEquals(
        new String[] {},
        testConf.programArguments.toArray(new String[] {}));

    Assert.assertArrayEquals(
        new String[] {
            "-Xdebug",
            "-Xrunjdwp:server=y,transport=dt_socket,address=9009,suspend=n",
            "-javaagent:org.jacoco.agent-0.7.5.201505241946-runtime.jar="
                + "append=true,dumponexit=true,"
                + "output=file,"
                + "destfile=equinoxtest\\jacoco.exec,"
                + "sessionid=equinoxtest_1447147594596"
        },
        parsablesConf.vmArguments.toArray(new String[] {}));
    Assert.assertArrayEquals(
        new String[] {
            "-configuration" },
        parsablesConf.programArguments.toArray(new String[] {}));
  }

  @Test
  public void testGetOverridedDistributionPackage() throws URISyntaxException {

    File distFolderFile = getDistFolderFile();

    EnvironmentType distIde = distSchemaProvider
        .getOverriddenDistributedEnvironmentConfig(distFolderFile, UseByType.IDE);

    Assert.assertNotNull(distIde);

    EnvironmentType distTest = distSchemaProvider
        .getOverriddenDistributedEnvironmentConfig(distFolderFile, UseByType.INTEGRATION_TEST);

    Assert.assertNotNull(distTest);

    EnvironmentType distParsables = distSchemaProvider
        .getOverriddenDistributedEnvironmentConfig(distFolderFile, UseByType.PARSABLES);

    Assert.assertNotNull(distParsables);

    assertSameInformation(distIde, distTest, distParsables);

    LaunchConfigType ideConf = distIde.getLaunchConfig();
    Assert.assertNull(ideConf.getOverrides());

    assertMap(toMap(ideConf.getVmArguments()),
        "javaagentJacoco", "-javaagent:org.jacoco.agent-0.7.5.201505241946-runtime.jar="
            + "append=true,dumponexit=true,"
            + "output=tcpserver,"
            + "destfile=equinoxtest\\jacoco.exec,"
            + "sessionid=equinoxtest_1447147594596");
    assertMap(toMap(ideConf.getProgramArguments()));

    LaunchConfigType testConf = distTest.getLaunchConfig();
    Assert.assertNull(testConf.getOverrides());
    assertMap(toMap(testConf.getVmArguments()),
        "javaagentJacoco", "-javaagent:org.jacoco.agent-0.7.5.201505241946-runtime.jar="
            + "append=true,dumponexit=true,"
            + "includes=org.everit.osgi.resource.*,"
            + "output=file,destfile=equinoxtest\\jacoco.exec,"
            + "sessionid=equinoxtest_1447147594596");
    assertMap(toMap(testConf.getProgramArguments()));

    LaunchConfigType parsablesConf = distParsables.getLaunchConfig();
    Assert.assertNull(parsablesConf.getOverrides());
    assertMap(toMap(parsablesConf.getVmArguments()),
        "javaagentJacoco", "-javaagent:org.jacoco.agent-0.7.5.201505241946-runtime.jar="
            + "append=true,dumponexit=true,"
            + "output=file,"
            + "destfile=equinoxtest\\jacoco.exec,"
            + "sessionid=equinoxtest_1447147594596",
        "debug", "-Xdebug",
        "debugport", "-Xrunjdwp:server=y,transport=dt_socket,address=9009,suspend=n");
    assertMap(toMap(parsablesConf.getProgramArguments()),
        "configuration", "-configuration");

  }

  private Map<String, String> toMap(final ArgumentsType programArguments) {
    return toMap(programArguments.getArgument());
  }

  private Map<String, String> toMap(final List<EntryType> entries) {
    Map<String, String> rval = new HashMap<String, String>();
    for (EntryType entry : entries) {
      String key = entry.getKey();
      String value = entry.getValue();
      rval.put(key, value);
    }
    return rval;
  }

}
