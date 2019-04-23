package com.bounteous.aem.compgenerator.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

public class GenerationConfig {

    private String name;

    private String title;

    private String group;

    private String type;

    private Options options;

    private String compDir;

    public String getName() {
        if(StringUtils.isNotBlank(name)){
            return name.replaceAll("[^a-z0-9+]", "-");
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJavaFormatedName() {
        if(StringUtils.isNotBlank(name)){
            return CaseUtils.toCamelCase(name.replaceAll("-", " "), true);
        }
        return name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public String getCompDir() {
        return compDir;
    }

    public void setCompDir(String compDir) {
        this.compDir = compDir;
    }
}
