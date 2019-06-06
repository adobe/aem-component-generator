/*
 * #%L
 * AEM Component Generator
 * %%
 * Copyright (C) 2019 Bounteous
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

    @JsonProperty(value = "items")
    private List<Property> items;

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

    public List<Property> getItems() {
        return items;
    }

    public void setItems(List<Property> items) {
        this.items = items;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Property)) {
            return false;
        }

        Property property = (Property) obj;
        return property.getField().equals(this.getField()) && property.getType().equals(this.getType());
    }

    @Override
    public int hashCode() {
        return getField().hashCode() + getType().hashCode();
    }
}
