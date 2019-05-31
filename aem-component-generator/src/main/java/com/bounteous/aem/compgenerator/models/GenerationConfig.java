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

public class GenerationConfig implements BaseModel {

    @JsonProperty("name")
    private String name;

    @JsonProperty("title")
    private String title;

    @JsonProperty("group")
    private String group;

    @JsonProperty("type")
    private String type;

    @JsonProperty("project-settings")
    private ProjectSettings projectSettings;

    @JsonProperty("options")
    private Options options;

    // Non-JSON property runtime variables
    private String compDir;
    private String javaFormatedName;


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
            javaFormatedName = CaseUtils.toCamelCase(name.replaceAll("-", " "), true);
        }
        return javaFormatedName;
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

    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    public void setProjectSettings(ProjectSettings projectSettings) {
        this.projectSettings = projectSettings;
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

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(type);
    }
}
