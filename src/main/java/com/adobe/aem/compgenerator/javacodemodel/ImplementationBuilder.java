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

import com.adobe.acs.commons.models.injectors.annotation.ChildResourceFromRequest;
import com.adobe.acs.commons.models.injectors.annotation.SharedValueMapValue;
import com.adobe.aem.compgenerator.Constants;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.adobe.aem.compgenerator.models.Property;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JConditional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sun.codemodel.JMod.PRIVATE;

public class ImplementationBuilder extends JavaCodeBuilder {
    private static final Logger LOG = LogManager.getLogger(ImplementationBuilder.class);
    private static final String INJECTION_STRATEGY = "injectionStrategy";
    private static final String OPTIONAL_INJECTION_STRATEGY = "OPTIONAL";
    private static final String SLING_MODEL_EXPORTER_NAME = "SLING_MODEL_EXPORTER_NAME";
    private static final String SLING_MODEL_EXTENSION = "SLING_MODEL_EXTENSION";

    private final String className;
    private final JClass interfaceClass;
    private final JPackage implPackage;
    private final String[] adaptables;
    private final boolean isAllowExporting;

    private Map<String, Boolean> fieldJsonExposeMap = new HashMap<>();
    private Map<String, String> fieldJsonPropertyMap = new HashMap<>();

    /**
     * Construct a new Sling Model implementation class.
     *
     * @param codeModel
     * @param generationConfig
     * @param className
     * @param interfaceClass
     */
    public ImplementationBuilder(JCodeModel codeModel,
                                 GenerationConfig generationConfig,
                                 String className,
                                 JClass interfaceClass) {
        super(codeModel, generationConfig);
        this.className = className;
        this.interfaceClass = interfaceClass;
        this.implPackage = codeModel._package(generationConfig.getProjectSettings().getModelImplPackage());
        this.adaptables = generationConfig.getOptions().getModelAdaptables();
        this.isAllowExporting = generationConfig.getOptions().isAllowExporting();
    }

    public void build(String resourceType) throws JClassAlreadyExistsException {
        JDefinedClass jc = this.implPackage._class(this.className)._implements(this.interfaceClass);
        addSlingAnnotations(jc, this.interfaceClass, resourceType);

        addFieldVars(jc, globalProperties, Constants.PROPERTY_TYPE_GLOBAL);
        addFieldVars(jc, sharedProperties, Constants.PROPERTY_TYPE_SHARED);
        addFieldVars(jc, privateProperties, Constants.PROPERTY_TYPE_PRIVATE);

        addGetters(jc);
        addExportedTypeMethod(jc);
    }

    private void addSlingAnnotations(JDefinedClass jDefinedClass, JClass adapterClass, String resourceType) {
        JAnnotationUse jAUse = jDefinedClass.annotate(codeModel.ref(Model.class));
        JAnnotationArrayMember adaptablesArray = jAUse.paramArray("adaptables");
        for (String adaptable : adaptables) {
            if ("resource".equalsIgnoreCase(adaptable)) {
                adaptablesArray.param(codeModel.ref(Resource.class));
            }
            if ("request".equalsIgnoreCase(adaptable)) {
                adaptablesArray.param(codeModel.ref(SlingHttpServletRequest.class));
            }
        }
        if (this.isAllowExporting) {
            jAUse.paramArray("adapters").param(adapterClass).param(codeModel.ref(ComponentExporter.class));
        } else {
            jAUse.param("adapters", adapterClass);
        }
        if (StringUtils.isNotBlank(resourceType)) {
            jAUse.param("resourceType", resourceType);
        }
        if (this.isAllowExporting) {
            jAUse = jDefinedClass.annotate(codeModel.ref(Exporter.class));
            jAUse.param("name", codeModel.ref(ExporterConstants.class).staticRef(SLING_MODEL_EXPORTER_NAME));
            jAUse.param("extensions", codeModel.ref(ExporterConstants.class).staticRef(SLING_MODEL_EXTENSION));
        }
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
            } else if (property.getItems().size() > 1) { // composite multifield
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
        String fieldType = JavaCodeModel.getFieldType(property);

        JClass fieldClass = property.getType().equalsIgnoreCase("multifield")
                ? codeModel.ref(fieldType).narrow(codeModel.ref(JavaCodeModel.getFieldType(property.getItems().get(0))))
                : codeModel.ref(fieldType);
        JFieldVar jFieldVar = jc.field(PRIVATE, fieldClass, property.getField());

        if (StringUtils.equalsIgnoreCase(property.getType(), "image")) {
            jFieldVar.annotate(codeModel.ref(ChildResourceFromRequest.class))
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

        setupFieldGetterAnnotations(jFieldVar, property);
    }

    /**
     * method that add the fieldname as private and adds a class to jc
     */
    private void addPropertyAndObjectAsPrivateField(JDefinedClass jc, Property property) {
        String modelClassName = JavaCodeModel.getMultifieldInterfaceName(property);

        // Create the multifield item
        if (!property.getUseExistingModel()) {
            buildImplementation(property.getItems(), modelClassName);
        }

        String fieldType = JavaCodeModel.getFieldType(property);
        JClass narrowedClass = codeModel.ref(generationConfig.getProjectSettings().getModelInterfacePackage() + "." + modelClassName);
        JClass fieldClass = codeModel.ref(fieldType).narrow(narrowedClass);
        JFieldVar jFieldVar = jc.field(PRIVATE, fieldClass, property.getField());
        jFieldVar.annotate(codeModel.ref(ChildResourceFromRequest.class))
                .param(INJECTION_STRATEGY,
                        codeModel.ref(InjectionStrategy.class).staticRef(OPTIONAL_INJECTION_STRATEGY));

        setupFieldGetterAnnotations(jFieldVar, property);
    }

    /**
     * adds getters to all the fields available in the java class.
     *
     * @param jc
     */
    private void addGetters(JDefinedClass jc) {
        Map<String, JFieldVar> fieldVars = jc.fields();
        if (!fieldVars.isEmpty()) {
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
     * @param jc
     * @param jFieldVar
     */
    private void addGetter(JDefinedClass jc, JFieldVar jFieldVar) {
        JMethod getMethod = jc.method(JMod.PUBLIC, jFieldVar.type(), getMethodFormattedString(jFieldVar.name()));
        getMethod.annotate(codeModel.ref(Override.class));

        if (this.isAllowExporting) {
            if (!this.fieldJsonExposeMap.get(jFieldVar.name())) {
                getMethod.annotate(codeModel.ref(JsonIgnore.class));
            }

            if (StringUtils.isNotBlank(this.fieldJsonPropertyMap.get(jFieldVar.name()))) {
                getMethod.annotate(codeModel.ref(JsonProperty.class))
                        .param("value", this.fieldJsonPropertyMap.get(jFieldVar.name()));
            }
        }


        if (jFieldVar.type().erasure().fullName().equals(List.class.getName())) {
            JExpression condition = new NotNullExpression(jFieldVar);
            JExpression ifTrue = codeModel.ref(Collections.class).staticInvoke("unmodifiableList").arg(jFieldVar);
            JExpression ifFalse = JExpr._null();
            getMethod.body()._return(new TernaryOperator(condition, ifTrue, ifFalse));
        } else {
            getMethod.body()._return(jFieldVar);
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

    private void buildImplementation(List<Property> properties, String modelClassName) {
        try {
            JClass childInterfaceClass = codeModel.ref(generationConfig.getProjectSettings().getModelInterfacePackage() + "." + modelClassName);
            JDefinedClass implClass = this.implPackage._class(modelClassName + "Impl")._implements(childInterfaceClass);
            addSlingAnnotations(implClass, childInterfaceClass, null);
            addFieldVars(implClass, properties, Constants.PROPERTY_TYPE_PRIVATE);
            addGetters(implClass);
            addExportedTypeMethod(implClass);
        } catch (JClassAlreadyExistsException ex) {
            LOG.error("Failed to generate child implementation classes.", ex);
        }
    }

    private void setupFieldGetterAnnotations(JFieldVar jFieldVar, Property property) {
        boolean isFieldJsonExpose = false;
        String fieldJsonPropertyValue = "";

        if (this.isAllowExporting) {
            isFieldJsonExpose = property.isShouldExporterExpose();
            fieldJsonPropertyValue = property.getJsonProperty();
        }

        this.fieldJsonExposeMap.put(jFieldVar.name(), isFieldJsonExpose);
        this.fieldJsonPropertyMap.put(jFieldVar.name(), fieldJsonPropertyValue);
    }

    private void addExportedTypeMethod(JDefinedClass jc) {
        if (this.isAllowExporting) {
            JFieldVar jFieldVar = jc.field(PRIVATE, codeModel.ref(Resource.class), "resource");
            jFieldVar.annotate(codeModel.ref(SlingObject.class));
            JMethod method = jc.method(JMod.PUBLIC, codeModel.ref(String.class), "getExportedType");
            method.annotate(codeModel.ref(Override.class));
            method.body()._return(jFieldVar.invoke("getResourceType"));
        }
    }
}
