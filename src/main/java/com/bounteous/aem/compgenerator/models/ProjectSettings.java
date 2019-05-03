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

public class ProjectSettings {

    @JsonProperty("model-interface-pkg")
    private String modelInterfacePackage;

    @JsonProperty("model-impl-pkg")
    private String modelImplPackage;

    @JsonProperty("component-path")
    private String componentPath;

    @JsonProperty("bundle-path")
    private String bundlePath;

    @JsonProperty("apps-path")
    private String appsPath;

    public String getModelInterfacePackage() {
        return modelInterfacePackage;
    }

    public void setModelInterfacePackage(String modelInterfacePackage) {
        this.modelInterfacePackage = modelInterfacePackage;
    }

    public String getModelImplPackage() {
        return modelImplPackage;
    }

    public void setModelImplPackage(String modelImplPackage) {
        this.modelImplPackage = modelImplPackage;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public String getBundlePath() {
        return bundlePath;
    }

    public void setBundlePath(String bundlePath) {
        this.bundlePath = bundlePath;
    }

    public String getAppsPath() {
        return appsPath;
    }

    public void setAppsPath(String appsPath) {
        this.appsPath = appsPath;
    }
}
