package com.adobe.aem.compgenerator.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tab implements BaseModel {

	@JsonProperty("field")
	private String field;

	@JsonProperty("type")
	private String type;

	@JsonProperty("label")
	private String label;

	@JsonProperty("properties")
	private List<Property> properties;

	@JsonProperty("properties-global")
	private List<Property> globalProperties;

	@JsonProperty("properties-shared")
	private List<Property> sharedProperties;

	public String getField() {
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
