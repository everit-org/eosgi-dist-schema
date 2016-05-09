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
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.05.09 at 09:47:08 AM CEST 
//


package org.everit.osgi.dev.eosgi.dist.schema.xsd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AbstractLaunchConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractLaunchConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="vmArguments" type="{http://everit.org/eosgi/dist/definition/4.0.0}ArgumentsType" minOccurs="0"/>
 *         &lt;element name="programArguments" type="{http://everit.org/eosgi/dist/definition/4.0.0}ArgumentsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractLaunchConfigType", propOrder = {
    "vmArguments",
    "programArguments"
})
@XmlSeeAlso({
    LaunchConfigOverrideType.class,
    LaunchConfigType.class
})
public abstract class AbstractLaunchConfigType {

    protected ArgumentsType vmArguments;
    protected ArgumentsType programArguments;

    /**
     * Gets the value of the vmArguments property.
     * 
     * @return
     *     possible object is
     *     {@link ArgumentsType }
     *     
     */
    public ArgumentsType getVmArguments() {
        return vmArguments;
    }

    /**
     * Sets the value of the vmArguments property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArgumentsType }
     *     
     */
    public void setVmArguments(ArgumentsType value) {
        this.vmArguments = value;
    }

    /**
     * Gets the value of the programArguments property.
     * 
     * @return
     *     possible object is
     *     {@link ArgumentsType }
     *     
     */
    public ArgumentsType getProgramArguments() {
        return programArguments;
    }

    /**
     * Sets the value of the programArguments property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArgumentsType }
     *     
     */
    public void setProgramArguments(ArgumentsType value) {
        this.programArguments = value;
    }

}
