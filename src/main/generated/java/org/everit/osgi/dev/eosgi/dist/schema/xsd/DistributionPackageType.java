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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.10.30 at 12:01:05 PM CET 
//


package org.everit.osgi.dev.eosgi.dist.schema.xsd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DistributionPackageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DistributionPackageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="artifacts" type="{http://everit.org/eosgi/dist/definition/4.0.0}ArtifactsType" minOccurs="0"/>
 *         &lt;element name="environmentConfiguration" type="{http://everit.org/eosgi/dist/definition/4.0.0}EnvironmentConfigurationType"/>
 *         &lt;element name="parsables" type="{http://everit.org/eosgi/dist/definition/4.0.0}ParsablesType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="environmentId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="frameworkStartLevel" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="bundleStartLevel" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DistributionPackageType", propOrder = {
    "artifacts",
    "environmentConfiguration",
    "parsables"
})
public class DistributionPackageType {

    protected ArtifactsType artifacts;
    protected EnvironmentConfigurationType environmentConfiguration;
    protected ParsablesType parsables;
    @XmlAttribute(name = "environmentId", required = true)
    protected String environmentId;
    @XmlAttribute(name = "frameworkStartLevel")
    protected Integer frameworkStartLevel;
    @XmlAttribute(name = "bundleStartLevel")
    protected Integer bundleStartLevel;

    /**
     * Gets the value of the artifacts property.
     * 
     * @return
     *     possible object is
     *     {@link ArtifactsType }
     *     
     */
    public ArtifactsType getArtifacts() {
        return artifacts;
    }

    /**
     * Sets the value of the artifacts property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArtifactsType }
     *     
     */
    public void setArtifacts(ArtifactsType value) {
        this.artifacts = value;
    }

    /**
     * Gets the value of the environmentConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link EnvironmentConfigurationType }
     *     
     */
    public EnvironmentConfigurationType getEnvironmentConfiguration() {
        return environmentConfiguration;
    }

    /**
     * Sets the value of the environmentConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvironmentConfigurationType }
     *     
     */
    public void setEnvironmentConfiguration(EnvironmentConfigurationType value) {
        this.environmentConfiguration = value;
    }

    /**
     * Gets the value of the parsables property.
     * 
     * @return
     *     possible object is
     *     {@link ParsablesType }
     *     
     */
    public ParsablesType getParsables() {
        return parsables;
    }

    /**
     * Sets the value of the parsables property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParsablesType }
     *     
     */
    public void setParsables(ParsablesType value) {
        this.parsables = value;
    }

    /**
     * Gets the value of the environmentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnvironmentId() {
        return environmentId;
    }

    /**
     * Sets the value of the environmentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnvironmentId(String value) {
        this.environmentId = value;
    }

    /**
     * Gets the value of the frameworkStartLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFrameworkStartLevel() {
        return frameworkStartLevel;
    }

    /**
     * Sets the value of the frameworkStartLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFrameworkStartLevel(Integer value) {
        this.frameworkStartLevel = value;
    }

    /**
     * Gets the value of the bundleStartLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBundleStartLevel() {
        return bundleStartLevel;
    }

    /**
     * Sets the value of the bundleStartLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBundleStartLevel(Integer value) {
        this.bundleStartLevel = value;
    }

}
