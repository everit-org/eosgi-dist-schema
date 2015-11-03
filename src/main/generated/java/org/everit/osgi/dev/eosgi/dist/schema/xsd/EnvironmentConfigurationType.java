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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EnvironmentConfigurationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EnvironmentConfigurationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mainJar" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mainClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="classPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="launchConfig" type="{http://everit.org/eosgi/dist/definition/4.0.0}LaunchConfigType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnvironmentConfigurationType", propOrder = {
    "mainJar",
    "mainClass",
    "classPath",
    "launchConfig"
})
public class EnvironmentConfigurationType {

    protected String mainJar;
    protected String mainClass;
    protected String classPath;
    protected LaunchConfigType launchConfig;

    /**
     * Gets the value of the mainJar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMainJar() {
        return mainJar;
    }

    /**
     * Sets the value of the mainJar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMainJar(String value) {
        this.mainJar = value;
    }

    /**
     * Gets the value of the mainClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * Sets the value of the mainClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMainClass(String value) {
        this.mainClass = value;
    }

    /**
     * Gets the value of the classPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassPath() {
        return classPath;
    }

    /**
     * Sets the value of the classPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassPath(String value) {
        this.classPath = value;
    }

    /**
     * Gets the value of the launchConfig property.
     * 
     * @return
     *     possible object is
     *     {@link LaunchConfigType }
     *     
     */
    public LaunchConfigType getLaunchConfig() {
        return launchConfig;
    }

    /**
     * Sets the value of the launchConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link LaunchConfigType }
     *     
     */
    public void setLaunchConfig(LaunchConfigType value) {
        this.launchConfig = value;
    }

}
