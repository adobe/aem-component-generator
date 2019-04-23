package com.bounteous.aem.compgenerator.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Options {

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
    private List<Property> gobalProperties;

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

    public List<Property> getGobalProperties() {
        return gobalProperties;
    }

    public void setGobalProperties(List<Property> gobalProperties) {
        this.gobalProperties = gobalProperties;
    }
}
