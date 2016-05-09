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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LaunchConfigOverrideType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LaunchConfigOverrideType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://everit.org/eosgi/dist/definition/4.0.0}AbstractLaunchConfigType">
 *       &lt;sequence>
 *         &lt;element name="useBy" type="{http://everit.org/eosgi/dist/definition/4.0.0}UseByType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LaunchConfigOverrideType", propOrder = {
    "useBy"
})
public class LaunchConfigOverrideType
    extends AbstractLaunchConfigType
{

    @XmlElement(required = true)
    protected UseByType useBy;

    /**
     * Gets the value of the useBy property.
     * 
     * @return
     *     possible object is
     *     {@link UseByType }
     *     
     */
    public UseByType getUseBy() {
        return useBy;
    }

    /**
     * Sets the value of the useBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link UseByType }
     *     
     */
    public void setUseBy(UseByType value) {
        this.useBy = value;
    }

}
