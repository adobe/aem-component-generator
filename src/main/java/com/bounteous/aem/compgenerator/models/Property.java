package com.bounteous.aem.compgenerator.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.util.Map;

public class Property {

    @JsonProperty("field")
    private String field;

    @JsonProperty("type")
    private String type;

    @JsonProperty("label")
    private String label;

    @JsonProperty("annotate")
    private String annotate;

    @JsonProperty("attributes")
    private Map<String, String> attributes;

    public String getField() {
        if (StringUtils.isNotBlank(field)) {
            return CaseUtils.toCamelCase(field.replaceAll("[^a-z0-9+]", " "), false);
        }
        return field;
    }

    public String getFieldGetterName() {
        if (StringUtils.isNotBlank(field)) {
            return CaseUtils.toCamelCase(field.replaceAll("[^a-z0-9+]", " "), true);
        }
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAnnotate() {
        return annotate;
    }

    public void setAnnotate(String annotate) {
        this.annotate = annotate;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

}
