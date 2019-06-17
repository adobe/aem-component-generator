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
package com.bounteous.aem.compgenerator.javacodemodel;

import com.adobe.acs.commons.models.injectors.annotation.SharedValueMapValue;
import com.bounteous.aem.compgenerator.Constants;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.bounteous.aem.compgenerator.models.Property;
import com.bounteous.aem.compgenerator.utils.CommonUtils;
import com.hs2solutions.aem.base.core.models.annotations.injectorspecific.ChildRequest;
import com.sun.codemodel.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bounteous.aem.compgenerator.utils.CommonUtils.getResourceContentAsString;
import static com.sun.codemodel.JMod.NONE;
import static com.sun.codemodel.JMod.PRIVATE;


/**
 * Root of the code.
 *
 *
 * Here's your JavaCodeModel application.
 *
 * <pre>
 * JavaCodeModel jcm = new JavaCodeModel();
 *
 * // generate source code and write them from jcm.
 * jcm.buildSlingModel(generationConfig);
 * ...
 * </pre>
 *
 * JavaCodeModel creates source code of your sling-model interface and implementation
 * using user data config configuration object.
 *
 */
public class JavaCodeModel {
    private static final Logger LOG = LogManager.getLogger(JavaCodeModel.class);
    private static final String INJECTION_STRATEGY = "injectionStrategy";
    private static final String OPTIONAL_INJECTION_STRATEGY = "OPTIONAL";

    private JCodeModel codeModel;
    private JDefinedClass jc;
    private GenerationConfig generationConfig;

    private List<Property> globalProperties;
    private List<Property> sharedProperties;
    private List<Property> privateProperties;

    /**
     * builds your slingModel interface and implementation class with all required
     * sling annotation, fields and getters based on the <code>generationConfig</code>.
     */
    public void buildSlingModel(GenerationConfig generationConfig) {
        this.codeModel = new JCodeModel();
        this.generationConfig = generationConfig;
        setProperties();
        buildInterface();
        buildImplClass();
        LOG.info( () -> "Starting to build multifield interfaces and classes." );

        buildChildInterfacesAndClasses();
        generateCodeFiles();
        LOG.info("--------------* Sling Model successfully generated *--------------");
    }

    /**
     * builds your slingModel interface with all required annotation,
     * fields and getters based on the <code>generationConfig</code>.
     */
    private void buildInterface() {
        try {
            JPackage jPackage = codeModel._package(generationConfig.getProjectSettings().getModelInterfacePackage());
            jc = jPackage._interface(generationConfig.getJavaFormatedName());
            String comment = "Defines the {@code "
                    + generationConfig.getJavaFormatedName()
                    + "} Sling Model used for the {@code "
                    + CommonUtils.getResourceType(generationConfig)
                    + "} component.";
            jc.javadoc().append(comment);
            jc.annotate(codeModel.ref("aQute.bnd.annotation.ConsumerType"));

            addGettersWithoutFields(jc, globalProperties);
            addGettersWithoutFields(jc, sharedProperties);
            addGettersWithoutFields(jc, privateProperties);
        } catch (JClassAlreadyExistsException e) {
            LOG.error(e);
        }
    }

    /**
     * builds your slingModel implementation with all required sling annotation,
     * fields and getters based on the <code>generationConfig</code>.
     */
    private void buildImplClass() {
        try {
            JPackage jPackage = codeModel._package(generationConfig.getProjectSettings().getModelImplPackage());
            JDefinedClass jcInterface = jc;
            jc = jPackage._class(generationConfig.getJavaFormatedName() + "Impl")
                    ._implements(jcInterface);
            jc = addSlingAnnotations(jc, jcInterface);

            addFieldVars(jc, globalProperties, Constants.PROPERTY_TYPE_GLOBAL);
            addFieldVars(jc, sharedProperties, Constants.PROPERTY_TYPE_SHARED);
            addFieldVars(jc, privateProperties, Constants.PROPERTY_TYPE_PRIVATE);

            addGetters(jc);

        } catch (JClassAlreadyExistsException e) {
            LOG.error(e);
        }
    }

    private void buildChildInterfacesAndClasses() {
        List<Property> props = generationConfig.getOptions().getProperties();
        props.stream().filter(Objects::nonNull)
                .forEach(prop -> {
                    if (prop.getType().equalsIgnoreCase("multifield")
                            && prop.getItems().size() > 1) {
                        try {
                            JPackage jPackage = codeModel._package(generationConfig.getProjectSettings().getModelInterfacePackage());
                            JDefinedClass interfaceClass = jPackage._interface(StringUtils.capitalize(prop.getField()) + "Multifield");
                            String comment = "Defines the {@code "
                                    + StringUtils.capitalize(prop.getField()) + "Multifield"
                                    + "} Sling Model used for the multifield in {@code "
                                    + CommonUtils.getResourceType(generationConfig)
                                    + "} component.";
                            interfaceClass.javadoc().append(comment);
                            interfaceClass.annotate(codeModel.ref("aQute.bnd.annotation.ConsumerType"));
                            addGettersWithoutFields(interfaceClass, prop.getItems());

                            jPackage = codeModel._package(generationConfig.getProjectSettings().getModelImplPackage());
                            JDefinedClass implClass = jPackage._class(StringUtils.capitalize(prop.getField()) + "MultifieldImpl")
                                    ._implements(interfaceClass);
                            JAnnotationUse jau = implClass.annotate(codeModel.ref(Model.class));
                            jau.param("adapters", interfaceClass);
                            jau.paramArray("adaptables")
                                    .param(codeModel.ref(Resource.class))
                                    .param(codeModel.ref(SlingHttpServletRequest.class));
                            addFieldVars(implClass, prop.getItems(), Constants.PROPERTY_TYPE_PRIVATE);
                            addGetters(implClass);
                        } catch (JClassAlreadyExistsException e) {
                            LOG.error("Failed to generate child interface and implementation classes.", e);
                        }
                    }
                });

    }

    /**
     * Generates the slingModel file based on values from the config and the current codeModel object.
     */
    private void generateCodeFiles() {
        try {
            // RenameFileCodeWritern to rename existing files
            CodeWriter codeWriter = new RenameFileCodeWriter(new File(generationConfig.getProjectSettings().getBundlePath()));
            // PrologCodeWriter to prepend the copyright template in each file
            PrologCodeWriter prologCodeWriter = new PrologCodeWriter(codeWriter,
                    getResourceContentAsString(Constants.TEMPLATE_COPYRIGHT_JAVA));

            codeModel.build(prologCodeWriter);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    /**
     * adds all default sling annotations to class.
     *
     * @param jDefinedClass
     * @param jcInterface
     * @return
     */
    private JDefinedClass addSlingAnnotations(JDefinedClass jDefinedClass, JDefinedClass jcInterface) {
        if (jDefinedClass != null) {
            jDefinedClass.annotate(codeModel.ref(Model.class))
                    .param("adapters", jcInterface.getPackage()._getClass(generationConfig.getJavaFormatedName()))
                    .param("resourceType", CommonUtils.getResourceType(generationConfig))
                    .paramArray("adaptables")
                    .param(codeModel.ref(Resource.class))
                    .param(codeModel.ref(SlingHttpServletRequest.class));
        }
        return jDefinedClass;
    }

    /**
     * adds fields to java model.
     *
     * @param properties
     * @param propertyType
     */
    private void addFieldVars(JDefinedClass jc, List<Property> properties, final String propertyType) {
        properties.stream()
                .filter(Objects::nonNull)
                .forEach(property -> addFieldVar(jc, property, propertyType));
    }

    /**
     * add field variable to to jc.
     *
     * @param property
     */
    private void addFieldVar(JDefinedClass jc, Property property, final String propertyType) {
        if (property != null && StringUtils.isNotBlank(property.getField())) {
            if (!property.getType().equalsIgnoreCase("multifield")) { // non multifield properties
                addPropertyAsPrivateField(jc, property, propertyType);
            } else if (property.getItems().size() == 1) { // multifield with single property
                addPropertyAsPrivateField(jc, property, propertyType);
            } else if (property.getItems().size() > 1) {
                addPropertyAndObjectAsPrivateField(jc, property);
            }
        }
    }

    /**
     * method that add the fieldname as private to jc.
     *
     * @param property
     * @param propertyType
     */
    private void addPropertyAsPrivateField(JDefinedClass jc, Property property, final String propertyType) {
        String fieldType = getFieldType(property);
        if (jc.isClass()) {
            JClass fieldClass = property.getType().equalsIgnoreCase("multifield")
                    ? codeModel.ref(fieldType).narrow(codeModel.ref(getFieldType(property.getItems().get(0))))
                    : codeModel.ref(fieldType);
            JFieldVar jFieldVar = jc.field(PRIVATE, fieldClass, property.getField());

            if (StringUtils.equalsIgnoreCase(property.getType(), "image")) {
                jFieldVar.annotate(codeModel.ref(ChildRequest.class))
                        .param(INJECTION_STRATEGY,
                                codeModel.ref(InjectionStrategy.class).staticRef(OPTIONAL_INJECTION_STRATEGY));

            } else if (StringUtils.equalsIgnoreCase(propertyType, Constants.PROPERTY_TYPE_PRIVATE)) {
                jFieldVar.annotate(codeModel.ref(ValueMapValue.class))
                        .param(INJECTION_STRATEGY,
                                codeModel.ref(InjectionStrategy.class).staticRef(OPTIONAL_INJECTION_STRATEGY));
            } else {
                jFieldVar.annotate(codeModel.ref(SharedValueMapValue.class))
                        .param(INJECTION_STRATEGY,
                                codeModel.ref(InjectionStrategy.class).staticRef(OPTIONAL_INJECTION_STRATEGY));
            }
        } else if (jc.isInterface()) {
            jc.field(NONE, codeModel.ref(fieldType), property.getField());
        }
    }

    /**
     * method that add the fieldname as private and adds a class to jc
     */
    private void addPropertyAndObjectAsPrivateField(JDefinedClass jc, Property property) {
        if (jc.isClass()) {
            String fieldType = getFieldType(property);
            JClass narrowedClass = codeModel.ref(generationConfig.getProjectSettings().getModelInterfacePackage() + "." + StringUtils.capitalize(property.getField()) + "Multifield");
            JClass fieldClass = codeModel.ref(fieldType).narrow(narrowedClass);
            JFieldVar jFieldVar = jc.field(PRIVATE, fieldClass, property.getField());
            jFieldVar.annotate(codeModel.ref(ChildRequest.class))
                    .param(INJECTION_STRATEGY,
                            codeModel.ref(InjectionStrategy.class).staticRef(OPTIONAL_INJECTION_STRATEGY));
        }
    }

    /**
     * adds getters to all the fields available in the java class.
     */
    private void addGetters(JDefinedClass jc) {
        Map<String, JFieldVar> fieldVars = jc.fields();
        if (fieldVars.size() > 0) {
            for (Map.Entry<String, JFieldVar> entry : fieldVars.entrySet()) {
                if (entry.getValue() != null) {
                    addGetter(jc, entry.getValue());
                }
            }
        }
    }

    /**
     * add getter method for jFieldVar passed in.
     *
     * @param jFieldVar
     */
    private void addGetter(JDefinedClass jc, JFieldVar jFieldVar) {
        if (jc.isClass()) {
            JMethod getMethod = jc.method(JMod.PUBLIC, jFieldVar.type(), getMethodFormattedString(jFieldVar.name()));
            getMethod.annotate(codeModel.ref(Override.class));
            getMethod.body()._return(jFieldVar);
        } else {
            jc.method(NONE, jFieldVar.type(), getMethodFormattedString(jFieldVar.name()));
        }
    }

    /**
     * get the java fieldType based on the type input in the generationConfig
     *
     * @param property
     * @return String returns relevant java type of string passed in.
     */
    private String getFieldType(Property property) {
        String type = property.getType();
        if (type.equalsIgnoreCase("textfield")
                || type.equalsIgnoreCase("pathfield")
                || type.equalsIgnoreCase("textarea")
                || type.equalsIgnoreCase("hidden")
                || type.equalsIgnoreCase("select")
                || type.equalsIgnoreCase("radiogroup")) {
            return "java.lang.String";
        } else if (type.equalsIgnoreCase("numberfield")) {
            return "java.lang.Long";
        } else if (type.equalsIgnoreCase("checkbox")) {
            return "java.lang.Boolean";
        } else if (type.equalsIgnoreCase("datepicker")) {
            return "java.util.Calendar";
        } else if (type.equalsIgnoreCase("image")) {
            return "com.hs2solutions.aem.base.core.models.HS2Image";
        } else if (type.equalsIgnoreCase("multifield")) {
            return "java.util.List";
        }
        return type;
    }

    /**
     * method just adds getters based on the properties of generationConfig
     *
     * @param properties
     */
    private void addGettersWithoutFields(JDefinedClass jc, List<Property> properties) {
        if (properties != null && !properties.isEmpty()) {
            properties.forEach(property -> {
                JMethod method = jc.method(NONE, getGetterMethodReturnType(property), Constants.STRING_GET + property.getFieldGetterName());
                addJavadocToMethod(method, property);
            });
        }
    }

    /**
     * builds method name out of field variable.
     *
     * @param fieldVariable
     * @return String returns formatted getter method name.
     */
    private String getMethodFormattedString(String fieldVariable) {
        if (StringUtils.isNotBlank(fieldVariable) && StringUtils.length(fieldVariable) > 0) {
            return Constants.STRING_GET + Character.toTitleCase(fieldVariable.charAt(0)) + fieldVariable.substring(1);
        }
        return fieldVariable;
    }

    private JClass getGetterMethodReturnType(final Property property) {
        String fieldType = getFieldType(property);
        if (property.getType().equalsIgnoreCase("multifield")) {
            if (property.getItems().size() == 1) {
                return codeModel.ref(fieldType).narrow(codeModel.ref(getFieldType(property.getItems().get(0))));
            } else {
                return codeModel.ref(fieldType).narrow(codeModel.ref(StringUtils.capitalize(property.getField()) + "Multifield"));
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

    /**
     * Sets property attributes
     */
    private void setProperties() {
        Set<Property> occurredProperties = new HashSet<>();

        globalProperties = filterProperties(occurredProperties, generationConfig.getOptions().getGlobalProperties());
        occurredProperties.addAll(globalProperties);

        sharedProperties = filterProperties(occurredProperties, generationConfig.getOptions().getSharedProperties());
        occurredProperties.addAll(sharedProperties);

        privateProperties = filterProperties(occurredProperties, generationConfig.getOptions().getProperties());
        occurredProperties.addAll(privateProperties);
    }

    /**
     * Filters the given properties for invalid fields and returns all that are not contained in occurredProperties.
     *
     * @param occurredProperties
     * @param originalProperties
     * @return filtered properties
     */
    private List<Property> filterProperties(Set<Property> occurredProperties, List<Property> originalProperties) {
        List<Property> properties;
        if (originalProperties != null) {
            properties = originalProperties.stream()
                    .filter(Objects::nonNull)
                    .filter(property -> StringUtils.isNotBlank(property.getField()))
                    .filter(property -> StringUtils.isNotBlank(property.getType()))
                    .filter(property -> !(occurredProperties.contains(property)))
                    .collect(Collectors.toList());
        } else {
            properties = Collections.emptyList();
        }
        return properties;
    }
}
