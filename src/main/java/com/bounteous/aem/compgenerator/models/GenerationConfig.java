package com.bounteous.aem.compgenerator.models;

public class GenerationConfig {

    private String name;

    private String title;

    private String group;

    private String type;

    private Options options;

    private String compDir;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
