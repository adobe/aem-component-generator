/*
 * #%L
 * AEM Component Generator
 * %%
 * Copyright (C) 2019 Adobe
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
package com.adobe.aem.compgenerator.javacodemodel;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.aem.compgenerator.Constants;
import com.adobe.aem.compgenerator.utils.CommonUtils;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.adobe.aem.compgenerator.models.Property;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

import static com.sun.codemodel.JMod.NONE;
import static com.adobe.aem.compgenerator.javacodemodel.JavaCodeModel.getFieldType;

/**
 * <p>
 * Manages generating the necessary details to create the sling model interface.
 * </p>
 */
public class InterfaceBuilder extends JavaCodeBuilder {
    private static final Logger LOG = LogManager.getLogger(InterfaceBuilder.class);

    private final boolean isAllowExporting;
    private String interfaceClassName;

    /**
     * Construct a interface class builder
     *
     * @param codeModel        The {@link JCodeModel codeModel}
     * @param generationConfig The {@link GenerationConfig generationConfig}
     * @param interfaceName    The name of the interface
     */
    public InterfaceBuilder(JCodeModel codeModel, GenerationConfig generationConfig, String interfaceName) {
        super(codeModel, generationConfig);
        this.interfaceClassName = interfaceName;
        this.isAllowExporting = generationConfig.getOptions().isAllowExporting();
    }

    /**
     * Builds the interface class based on the configuration file.
     *
     * @return reference to the Interface
     */
    public JDefinedClass build() {
        String comment = "Defines the {@code "
                + generationConfig.getJavaFormatedName()
                + "} Sling Model used for the {@code "
                + CommonUtils.getResourceType(generationConfig)
                + "} component.";

        return buildInterface(this.interfaceClassName, comment, globalProperties, sharedProperties, privateProperties);
    }

    /**
     * method just adds getters based on the properties of generationConfig
     *
     * @param jc         the interface class
     * @param properties the list of properties
     */
    private void addGettersWithoutFields(JDefinedClass jc, List<Property> properties) {
        if (properties != null && !properties.isEmpty()) {
            properties.stream()
                    .filter(Objects::nonNull)
                    .forEach(property -> {
                        JMethod method = jc.method(NONE, getGetterMethodReturnType(property), Constants.STRING_GET + property.getFieldGetterName());
                        addJavadocToMethod(method, property);

                        if (this.isAllowExporting) {
                            if (!property.isShouldExporterExpose()) {
                                method.annotate(codeModel.ref(JsonIgnore.class));
                            }

                            if (StringUtils.isNotBlank(property.getJsonProperty())) {
                                method.annotate(codeModel.ref(JsonProperty.class))
                                        .param("value", property.getJsonProperty());
                            }
                        }

                        if (property.getType().equalsIgnoreCase("multifield")
                                && property.getItems().size() > 1) {
                            buildMultifieldInterface(property);
                        }
                    });
        }
    }

    private void buildMultifieldInterface(Property property) {
        if (!property.getUseExistingModel()) {
            String modelInterfaceName = JavaCodeModel.getMultifieldInterfaceName(property);
            String childComment = "Defines the {@code "
                    + modelInterfaceName
                    + "} Sling Model used for the multifield in {@code "
                    + CommonUtils.getResourceType(generationConfig)
                    + "} component.";

            buildInterface(modelInterfaceName, childComment, property.getItems());
        }
    }

    @SafeVarargs
    private final JDefinedClass buildInterface(String interfaceName, String comment, List<Property>... propertiesLists) {
        try {
            JPackage jPackage = codeModel._package(generationConfig.getProjectSettings().getModelInterfacePackage());
            JDefinedClass interfaceClass = jPackage._interface(interfaceName);
            interfaceClass.javadoc().append(comment);
            interfaceClass.annotate(codeModel.ref("org.osgi.annotation.versioning.ConsumerType"));

            if (this.isAllowExporting) {
                interfaceClass._extends(codeModel.ref(ComponentExporter.class));
            }
            if (propertiesLists != null) {
                for (List<Property> properties : propertiesLists) {
                    addGettersWithoutFields(interfaceClass, properties);
                }
            }
            return interfaceClass;
        } catch (JClassAlreadyExistsException e) {
            LOG.error("Failed to generate child interface.", e);
        }

        return null;
    }

    /**
     * Gets the return type of the getter method based on what type of property it is referring to.
     *
     * @param property the property for which the return type is calculated.
     * @return the type being returned by the getter
     */
    private JType getGetterMethodReturnType(final Property property) {
        String fieldType = getFieldType(property);
        if (property.getType().equalsIgnoreCase("multifield")) {
            if (property.getItems().size() == 1) {
                return codeModel.ref(fieldType).narrow(codeModel.ref(getFieldType(property.getItems().get(0))));
            } else {
                String narrowedClassName = StringUtils.defaultString(property.getModelName(),
                        CaseUtils.toCamelCase(property.getField(), true) + "Multifield");
                return codeModel.ref(fieldType).narrow(codeModel.ref(narrowedClassName));
            }
        } else {
            return codeModel.ref(fieldType);
        }
    }

    /**
     * Adds Javadoc to the method based on the information in the property and the generation config options.
     *
     * @param method
     * @param property
     */
    private void addJavadocToMethod(JMethod method, Property property) {
        JDocComment javadoc = method.javadoc();
        if (StringUtils.isNotBlank(property.getJavadoc())) {
            javadoc.append(property.getJavadoc());
            javadoc.append("\n\n@return " + getGetterMethodReturnType(property).name());
        } else if (generationConfig.getOptions() != null && generationConfig.getOptions().isHasGenericJavadoc()) {
            javadoc.append("Get the " + property.getField() + ".");
            javadoc.append("\n\n@return " + getGetterMethodReturnType(property).name());
        }
    }
}
