package com.adobe.aem.compgenerator.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tab implements BaseModel {

    @JsonProperty("id")
    private String id;

    @JsonProperty("label")
    private String label;

    @JsonProperty("fields")
    private List<String> fields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    @Override
    public boolean isValid() {
        return true;
    }

}
