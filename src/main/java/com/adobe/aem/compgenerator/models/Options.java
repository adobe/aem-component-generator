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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class Options implements BaseModel {

    private final static String[] DEFAULT_ADAPTABLES = new String[] { "request" };

    @JsonProperty("generic-javadoc")
    private boolean hasGenericJavadoc;

    @JsonProperty("js")
    private boolean hasJs;

    @JsonProperty("jstxt")
    private boolean hasJsTxt;

    @JsonProperty("css")
    private boolean hasCss;

    @JsonProperty("csstxt")
    private boolean hasCssTxt;

    @JsonProperty("html")
    private boolean hasHtml;

    @JsonProperty("slingmodel")
    private boolean hasSlingModel;

    @JsonProperty("content-exporter")
    private boolean allowExporting;

    @JsonProperty("model-adaptables")
    private String[] modelAdaptables;

    @JsonProperty(value = "html-content")
    private boolean htmlContent;

    @JsonProperty("properties")
    private List<Property> properties;

    @JsonProperty("properties-global")
    private List<Property> globalProperties;

    @JsonProperty("properties-shared")
    private List<Property> sharedProperties;

    @JsonProperty("properties-tabs")
    private List<Tab> tabProperties;

    @JsonProperty("properties-shared-tabs")
    private List<Tab> sharedTabProperties;

    @JsonProperty("properties-global-tabs")
    private List<Tab> globalTabProperties;

    public boolean isHasGenericJavadoc() {
        return hasGenericJavadoc;
    }

    public void setHasGenericJavadoc(boolean hasGenericJavadoc) {
        this.hasGenericJavadoc = hasGenericJavadoc;
    }

    public boolean isHasJs() {
        return hasJs;
    }

    public void setHasJs(boolean hasJs) {
        this.hasJs = hasJs;
    }

    public boolean isHasJsTxt() {
        return hasJsTxt;
    }

    public void setHasJsTxt(final boolean hasJsTxt) {
        this.hasJsTxt = hasJsTxt;
    }

    public boolean isHasCss() {
        return hasCss;
    }

    public void setHasCss(boolean hasCss) {
        this.hasCss = hasCss;
    }

    public boolean isHasCssTxt() {
        return hasCssTxt;
    }

    public void setHasCssTxt(final boolean hasCssTxt) {
        this.hasCssTxt = hasCssTxt;
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

    public boolean isAllowExporting() {
        return this.allowExporting;
    }

    public void setAllowExporting(boolean allowExporting) {
        this.allowExporting = allowExporting;
    }

    public String[] getModelAdaptables() {
        if (ArrayUtils.isEmpty(modelAdaptables)) {
            return DEFAULT_ADAPTABLES;
        }
        return modelAdaptables;
    }

    public void setModelAdaptables(final String[] modelAdaptables) {
        this.modelAdaptables = modelAdaptables;
    }

    public boolean isHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(boolean htmlContent) {
        this.htmlContent = htmlContent;
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

    public List<Tab> getTabProperties() {
        return tabProperties;
    }

    public void setTabProperties(List<Tab> tabProperties) {
        this.tabProperties = tabProperties;
    }

    public List<Tab> getSharedTabProperties() {
        return sharedTabProperties;
    }

    public void setSharedTabProperties(List<Tab> sharedTabProperties) {
        this.sharedTabProperties = sharedTabProperties;
    }

    public List<Tab> getGlobalTabProperties() {
        return globalTabProperties;
    }

    public void setGlobalTabProperties(List<Tab> globalTabProperties) {
        this.globalTabProperties = globalTabProperties;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
