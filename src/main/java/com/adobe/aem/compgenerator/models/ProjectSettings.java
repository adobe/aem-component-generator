/*
 * #%L
 * AEM Component Generator
 * %%
 * Copyright (C) 2019 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.aem.compgenerator.models;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public class ProjectSettings implements BaseModel {

    @JsonProperty("code-owner")
    private String codeOwner;

    @JsonProperty("copyright-year")
    private String year = "" + Calendar.getInstance().get(Calendar.YEAR);

    @JsonProperty("model-interface-pkg")
    private String modelInterfacePackage;

    @JsonProperty("model-impl-pkg")
    private String modelImplPackage;

    @JsonProperty("component-path")
    private String componentPath;

    @JsonProperty("bundle-path")
    private String bundlePath;

    @JsonProperty("test-path")
    private String testPath;

    @JsonProperty("apps-path")
    private String appsPath;

    public String getCodeOwner() {
        return codeOwner;
    }

    public void setCodeOwner(final String codeOwner) {
        this.codeOwner = codeOwner;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
        if (StringUtils.isBlank(this.year)) {
            this.year = "" + Calendar.getInstance().get(Calendar.YEAR);
        }
    }

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

    public String getTestPath() {
        return testPath;
    }

    public void setTestPath(String testPath) {
        this.testPath = testPath;
    }

    public String getAppsPath() {
        return appsPath;
    }

    public void setAppsPath(String appsPath) {
        this.appsPath = appsPath;
    }

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(modelInterfacePackage) && StringUtils.isNotBlank(modelImplPackage)
                && StringUtils.isNotBlank(componentPath) && StringUtils.isNotBlank(bundlePath) && StringUtils.isNotBlank(appsPath);
    }
}
