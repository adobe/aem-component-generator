package com.adobe.aem.compgenerator.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(id) && fields != null && !fields.isEmpty();
    }

}
