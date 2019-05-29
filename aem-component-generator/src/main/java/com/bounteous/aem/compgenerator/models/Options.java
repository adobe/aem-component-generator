/*
 * ***********************************************************************
 * BOUNTEOUS CONFIDENTIAL
 * ___________________
 *
 * Copyright 2019 Bounteous
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of Bounteous and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Bounteous
 * and its suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Bounteous.
 * ***********************************************************************
 */
package com.bounteous.aem.compgenerator.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Options implements BaseModel {

    @JsonProperty("js")
    private boolean hasJs;

    @JsonProperty("css")
    private boolean hasCss;

    @JsonProperty("html")
    private boolean hasHtml;

    @JsonProperty("slingmodel")
    private boolean hasSlingModel;

    @JsonProperty("properties")
    private List<Property> properties;

    @JsonProperty("properties-global")
    private List<Property> globalProperties;

    @JsonProperty("properties-shared")
    private List<Property> sharedProperties;

    public boolean isHasJs() {
        return hasJs;
    }

    public void setHasJs(boolean hasJs) {
        this.hasJs = hasJs;
    }

    public boolean isHasCss() {
        return hasCss;
    }

    public void setHasCss(boolean hasCss) {
        this.hasCss = hasCss;
    }

    public boolean isHasHtml() {
        return hasHtml;
    }

    public void setHasHtml(boolean hasHtml) {
        this.hasHtml = hasHtml;
    }

    public boolean isHasSlingModel() {
        return hasSlingModel;
    }

    public void setHasSlingModel(boolean hasSlingModel) {
        this.hasSlingModel = hasSlingModel;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<Property> getGlobalProperties() {
        return globalProperties;
    }

    public void setGlobalProperties(List<Property> globalProperties) {
        this.globalProperties = globalProperties;
    }

    public List<Property> getSharedProperties() {
        return sharedProperties;
    }

    public void setSharedProperties(List<Property> sharedProperties) {
        this.sharedProperties = sharedProperties;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
