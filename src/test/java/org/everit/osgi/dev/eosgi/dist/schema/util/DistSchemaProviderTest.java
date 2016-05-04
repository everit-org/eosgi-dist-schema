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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.everit.osgi.dev.eosgi.dist.schema.xsd.ArtifactType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.ArtifactsType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.BundleDataType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.DistributionPackageType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.LaunchConfigType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.ParsableType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.ParsablesType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.ProgramArgumentsType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.SystemPropertiesType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.UseByType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.VmArgumentsType;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

public class DistSchemaProviderTest {

  private final Comparator<ArtifactType> artifactTypeComparator = new Comparator<ArtifactType>() {

    @Override
    public int compare(final ArtifactType o1, final ArtifactType o2) {
      if ((o1 == null) && (o2 == null)) {
        return 0;
      }
      Assert.assertNotNull(o1);
      Assert.assertNotNull(o2);
      Assert.assertEquals(o1.getArtifactId(), o2.getArtifactId());
      assertEquals(o1.getBundle(), o2.getBundle(), bundleDataTypeComparator);
      Assert.assertEquals(o1.getClassifier(), o2.getClassifier());
      Assert.assertEquals(o1.getGroupId(), o2.getGroupId());
      Assert.assertEquals(o1.getTargetFile(), o2.getTargetFile());
      Assert.assertEquals(o1.getTargetFolder(), o2.getTargetFolder());
      Assert.assertEquals(o1.getType(), o2.getType());
      Assert.assertEquals(o1.getVersion(), o2.getVersion());
      return 0;
    }

  };
  private final Comparator<BundleDataType> bundleDataTypeComparator =
      new Comparator<BundleDataType>() {

        @Override
        public int compare(final BundleDataType o1, final BundleDataType o2) {
          if ((o1 == null) && (o2 == null)) {
            return 0;
          }
          Assert.assertNotNull(o1);
          Assert.assertNotNull(o2);
          Assert.assertEquals(o1.getLocation(), o2.getLocation());
          Assert.assertEquals(o1.getSymbolicName(), o2.getSymbolicName());
          Assert.assertEquals(o1.getVersion(), o2.getVersion());
          Assert.assertEquals(o1.getAction(), o2.getAction());
          Assert.assertEquals(o1.getStartLevel(), o2.getStartLevel());
          return 0;
        }
      };

  private final DistributedEnvironmentConfigurationProvider distSchemaProvider = new DistributedEnvironmentConfigurationProvider();

  private final Comparator<ParsableType> parsableTypeComparator = new Comparator<ParsableType>() {

    @Override
    public int compare(final ParsableType o1, final ParsableType o2) {
      if ((o1 == null) && (o2 == null)) {
        return 0;
      }
      Assert.assertNotNull(o1);
      Assert.assertNotNull(o2);
      Assert.assertEquals(o1.getEncoding(), o2.getEncoding());
      Assert.assertEquals(o1.getPath(), o2.getPath());
      return 0;
    }
  };

  private void assertArtifacts(final ArtifactsType artifacts1, final ArtifactsType artifacts2,
      final ArtifactsType artifacts3) {

    Assert.assertNotNull(artifacts1);
    Assert.assertNotNull(artifacts2);
    Assert.assertNotNull(artifacts3);

    assertEquals(
        artifacts1.getArtifact(),
        artifacts2.getArtifact(),
        artifactTypeComparator);
    assertEquals(
        artifacts2.getArtifact(),
        artifacts3.getArtifact(),
        artifactTypeComparator);
  }

  private <T> void assertEquals(final List<T> expectedList, final List<T> actualList,
      final Comparator<T> comparator) {
    Assert.assertEquals(expectedList.size(), actualList.size());
    for (int i = 0; i < expectedList.size(); i++) {
      comparator.compare(expectedList.get(i), actualList.get(i));
    }
  }

  private void assertEquals(final Object expected,
      final Object actual1, final Object actual2, final Object actual3) {
    Assert.assertEquals(expected, actual1);
    Assert.assertEquals(actual1, actual2);
    Assert.assertEquals(actual2, actual3);
  }

  private <T> void assertEquals(final T expected, final T actual, final Comparator<T> comparator) {
    comparator.compare(expected, actual);
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

    assertEquals(
        parsables1.getParsable(),
        parsables2.getParsable(),
        parsableTypeComparator);
    assertEquals(
        parsables2.getParsable(),
        parsables3.getParsable(),
        parsableTypeComparator);
  }

  private void assertSameInformation(
      final DistributionPackageType distIde,
      final DistributionPackageType distTest,
      final DistributionPackageType distParsables) {

    assertEquals("equinoxtest",
        distIde.getEnvironmentId(),
        distTest.getEnvironmentId(),
        distParsables.getEnvironmentId());
    assertEquals(null,
        distIde.getFrameworkStartLevel(),
        distTest.getFrameworkStartLevel(),
        distParsables.getFrameworkStartLevel());
    assertEquals(null,
        distIde.getBundleStartLevel(),
        distTest.getBundleStartLevel(),
        distParsables.getBundleStartLevel());

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

    DistributionPackageType ideDistributionPackage =
        distSchemaProvider.getOverriddenDistributionPackage(distFolderFile, UseByType.IDE);

    LaunchConfigurationDTO ideConf = distSchemaProvider
        .getLaunchConfiguration(ideDistributionPackage);
    Assert.assertNotNull(ideConf);

    DistributionPackageType integrationTestDistributionPackage =
        distSchemaProvider.getOverriddenDistributionPackage(distFolderFile,
            UseByType.INTEGRATION_TEST);

    LaunchConfigurationDTO testConf = distSchemaProvider
        .getLaunchConfiguration(integrationTestDistributionPackage);
    Assert.assertNotNull(testConf);

    DistributionPackageType parsablesDistributionPackage =
        distSchemaProvider.getOverriddenDistributionPackage(distFolderFile, UseByType.PARSABLES);

    LaunchConfigurationDTO parsablesConf = distSchemaProvider
        .getLaunchConfiguration(parsablesDistributionPackage);
    Assert.assertNotNull(parsablesConf);

    assertSameInformation(ideConf, testConf, parsablesConf);

    Assert.assertArrayEquals(
        new String[] {
            "-Dorg.osgi.framework.system.packages=javax.crypto.spec,javax.crypto,etc",
            "-Dfelix.cm.dir=../../../configadmin",
            "-Dorg.osgi.service.http.port=-1",
            "-Dorg.osgi.service.http.port.secure=4949",
            "-Dmvel2.disable.jit=true",
            "-Deosgi.environment.id=equinoxtest" },
        ideConf.systemProperties.toArray(new String[] {}));
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
            "-Dorg.osgi.framework.system.packages=javax.crypto.spec,javax.crypto,etc",
            "-Dfelix.cm.dir=../../../configadmin",
            "-Dorg.osgi.service.http.port=-1",
            "-Dorg.osgi.service.http.port.secure=0",
            "-Dmvel2.disable.jit=true",
            "-Deosgi.environment.id=equinoxtest" },
        testConf.systemProperties.toArray(new String[] {}));
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
            "-Dorg.osgi.framework.system.packages=javax.crypto.spec,javax.crypto,etc",
            "-Dfelix.cm.dir=../../../configadmin",
            "-Dorg.osgi.service.http.port=-1",
            "-Dorg.osgi.service.http.port.secure=4848",
            "-Dmvel2.disable.jit=true",
            "-Deosgi.environment.id=equinoxtest" },
        parsablesConf.systemProperties.toArray(new String[] {}));
    Assert.assertArrayEquals(
        new String[] {
            "-javaagent:org.jacoco.agent-0.7.5.201505241946-runtime.jar="
                + "append=true,dumponexit=true,"
                + "output=file,"
                + "destfile=equinoxtest\\jacoco.exec,"
                + "sessionid=equinoxtest_1447147594596",
            "-Xdebug",
            "-Xrunjdwp:server=y,transport=dt_socket,address=9009,suspend=n" },
        parsablesConf.vmArguments.toArray(new String[] {}));
    Assert.assertArrayEquals(
        new String[] {
            "-configuration" },
        parsablesConf.programArguments.toArray(new String[] {}));
  }

  @Test
  public void testGetOverridedDistributionPackage() throws URISyntaxException {

    File distFolderFile = getDistFolderFile();

    DistributionPackageType distIde = distSchemaProvider
        .getOverriddenDistributionPackage(distFolderFile, UseByType.IDE);

    Assert.assertNotNull(distIde);

    DistributionPackageType distTest = distSchemaProvider
        .getOverriddenDistributionPackage(distFolderFile, UseByType.INTEGRATION_TEST);

    Assert.assertNotNull(distTest);

    DistributionPackageType distParsables = distSchemaProvider
        .getOverriddenDistributionPackage(distFolderFile, UseByType.PARSABLES);

    Assert.assertNotNull(distParsables);

    assertSameInformation(distIde, distTest, distParsables);

    LaunchConfigType ideConf = distIde.getLaunchConfig();
    Assert.assertNull(ideConf.getOverrides());
    assertMap(toMap(ideConf.getSystemProperties()),
        "org.osgi.framework.system.packages", "javax.crypto.spec,javax.crypto,etc",
        "felix.cm.dir", "../../../configadmin",
        "org.osgi.service.http.port", "-1",
        "org.osgi.service.http.port.secure", "4949",
        "mvel2.disable.jit", "true",
        "eosgi.environment.id", "equinoxtest");
    assertMap(toMap(ideConf.getVmArguments()),
        "javaagentJacoco", "-javaagent:org.jacoco.agent-0.7.5.201505241946-runtime.jar="
            + "append=true,dumponexit=true,"
            + "output=tcpserver,"
            + "destfile=equinoxtest\\jacoco.exec,"
            + "sessionid=equinoxtest_1447147594596");
    assertMap(toMap(ideConf.getProgramArguments()));

    LaunchConfigType testConf = distTest.getLaunchConfig();
    Assert.assertNull(testConf.getOverrides());
    assertMap(toMap(testConf.getSystemProperties()),
        "org.osgi.framework.system.packages", "javax.crypto.spec,javax.crypto,etc",
        "felix.cm.dir", "../../../configadmin",
        "org.osgi.service.http.port", "-1",
        "org.osgi.service.http.port.secure", "0",
        "mvel2.disable.jit", "true",
        "eosgi.environment.id", "equinoxtest");
    assertMap(toMap(testConf.getVmArguments()),
        "javaagentJacoco", "-javaagent:org.jacoco.agent-0.7.5.201505241946-runtime.jar="
            + "append=true,dumponexit=true,"
            + "includes=org.everit.osgi.resource.*,"
            + "output=file,destfile=equinoxtest\\jacoco.exec,"
            + "sessionid=equinoxtest_1447147594596");
    assertMap(toMap(testConf.getProgramArguments()));

    LaunchConfigType parsablesConf = distParsables.getLaunchConfig();
    Assert.assertNull(parsablesConf.getOverrides());
    assertMap(toMap(parsablesConf.getSystemProperties()),
        "org.osgi.framework.system.packages", "javax.crypto.spec,javax.crypto,etc",
        "felix.cm.dir", "../../../configadmin",
        "org.osgi.service.http.port", "-1",
        "org.osgi.service.http.port.secure", "4848",
        "mvel2.disable.jit", "true",
        "eosgi.environment.id", "equinoxtest");
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

  private Map<String, String> toMap(final List<Object> any) {
    Map<String, String> rval = new HashMap<String, String>();
    for (Object object : any) {
      Node node = (Node) object;
      String key = node.getNodeName();
      String value = node.getTextContent();
      rval.put(key, value);
    }
    return rval;
  }

  private Map<String, String> toMap(final ProgramArgumentsType programArguments) {
    return toMap(programArguments.getAny());
  }

  private Map<String, String> toMap(final SystemPropertiesType systemProperties) {
    return toMap(systemProperties.getAny());
  }

  private Map<String, String> toMap(final VmArgumentsType vmArguments) {
    return toMap(vmArguments.getAny());
  }

}
