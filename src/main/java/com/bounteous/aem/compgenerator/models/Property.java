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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.util.List;
import java.util.Map;

public class Property implements BaseModel {

    @JsonProperty("field")
    private String field;

    @JsonProperty("type")
    private String type;

    @JsonProperty("label")
    private String label;

    @JsonProperty("description")
    private String description;

    @JsonProperty("attributes")
    private Map<String, String> attributes;

    @JsonProperty(value = "itemAttributes")
    private List<Property> itemAttributes;

    public String getField() {
        if (StringUtils.isNotBlank(field)) {
            return field;
        } else if (StringUtils.isNoneBlank(label)) {
            return CaseUtils.toCamelCase(label.replaceAll("[^A-Za-z0-9+]", " "), false);
        }
        return field;
    }

    public String getFieldGetterName() {
        if (StringUtils.isNotBlank(field)) {
            return StringUtils.capitalize(field);
        } else if (StringUtils.isNoneBlank(label)) {
            return CaseUtils.toCamelCase(label.replaceAll("[^A-Za-z0-9+]", " "), true);
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

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Property> getItemAttributes() {
        return itemAttributes;
    }

    public void setItemAttributes(List<Property> itemAttributes) {
        this.itemAttributes = itemAttributes;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
