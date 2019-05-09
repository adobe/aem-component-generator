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

package com.bounteous.aem.compgenerator.javacodemodel;

import com.adobe.acs.commons.models.injectors.annotation.SharedValueMapValue;
import com.bounteous.aem.compgenerator.Constants;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.bounteous.aem.compgenerator.models.Property;
import com.hs2solutions.aem.base.core.models.annotations.injectorspecific.ChildRequest;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FileCodeWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bounteous.aem.compgenerator.utils.CommonUtils.getResourceContentAsString;
import static com.sun.codemodel.JMod.NONE;
import static com.sun.codemodel.JMod.PRIVATE;


/**
 * Root of the code.
 *
 * <p>
 * Here's your JavaCodeModel application.
 *
 * <pre>
 * JavaCodeModel jcm = new JavaCodeModel();
 *
 * // generate source code and write them from jcm.
 * jcm._buildSlingModel(generationConfig);
 * ...
 *
 * <p>
 * JavaCodeModel creates source code of your sling-model interface and implementation
 * using user data config configuration object.
 */
public class JavaCodeModel {

    private JCodeModel codeModel;
    private JDefinedClass jc;
    GenerationConfig generationConfig;

    public JavaCodeModel() {
    }

    /**
     * builds your slingModel interface and implementation class with all required
     * sling annotation, fields and getters based on the <code>generationConfig</code>.
     */
    public void _buildSlingModel(GenerationConfig generationConfig) {
        this.codeModel = new JCodeModel();
        this.generationConfig = generationConfig;
        _buildInterface();
        _buildImplClass();
        System.out.println("--------------* Sling Model successfully generated *--------------");
    }

    /**
     * builds your slingModel interface with all required annotation,
     * fields and getters based on the <code>generationConfig</code>.
     */
    private void _buildInterface() {
        try {
            JPackage jPackage = codeModel._package(generationConfig.getProjectSettings().getModelInterfacePackage());
            jc = jPackage._interface(generationConfig.getJavaFormatedName());
            jc.annotate(codeModel.ref("aQute.bnd.annotation.ConsumerType"));

            if (generationConfig.getOptions().getGlobalProperties() != null) {
                _addGettersWithoutFields(generationConfig.getOptions().getGlobalProperties());
            }

            if (generationConfig.getOptions().getSharedProperties() != null) {
                _addGettersWithoutFields(generationConfig.getOptions().getSharedProperties());
            }

            if (generationConfig.getOptions().getProperties() != null) {
                _addGettersWithoutFields(generationConfig.getOptions().getProperties());
            }

            _generateCodeFile();

        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * builds your slingModel implementation with all required sling annotation,
     * fields and getters based on the <code>generationConfig</code>.
     */
    private void _buildImplClass() {
        try {
            JPackage jPackage = codeModel._package(generationConfig.getProjectSettings().getModelImplPackage());
            JDefinedClass jcInterface = jc;
            jc = jPackage._class(generationConfig.getJavaFormatedName() + "Impl")
                    ._implements(jcInterface);
            jc = _addSlingAnnotations(jc, jcInterface);

            if (generationConfig.getOptions().getGlobalProperties() != null) {
                _addFieldVars(generationConfig.getOptions().getGlobalProperties(), Constants.PROPERTY_TYPE_GLOBAL);
            }

            if (generationConfig.getOptions().getSharedProperties() != null) {
                _addFieldVars(generationConfig.getOptions().getSharedProperties(), Constants.PROPERTY_TYPE_SHARED);
            }

            if (generationConfig.getOptions().getProperties() != null) {
                _addFieldVars(generationConfig.getOptions().getProperties(), Constants.PROPERTY_TYPE_PRIVATE);
            }

            _addGetters();

            _generateCodeFile();

        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates the slingModel file based on values from the config and the current codeModel object.
     * @throws IOException - exception thrown when file is unable to be created.
     */
    private void _generateCodeFile() throws IOException {
        //Adding Class header comments to the class.
        CodeWriter codeWriter = new FileCodeWriter(new File(generationConfig.getProjectSettings().getBundlePath()));
        PrologCodeWriter prologCodeWriter = new PrologCodeWriter(codeWriter,
                getResourceContentAsString(Constants.TEMPLATE_COPYRIGHT_JAVA));

        codeModel.build(prologCodeWriter);
        System.out.println("Created : " + jc.fullName());
    }

    /**
     * adds all default sling annotations to class.
     *
     * @param jDefinedClass
     * @param jcInterface
     * @return
     */
    private JDefinedClass _addSlingAnnotations(JDefinedClass jDefinedClass, JDefinedClass jcInterface) {
        if (jDefinedClass != null) {
            jDefinedClass.annotate(codeModel.ref(Model.class))
                    .param("adapters", jcInterface.getPackage()._getClass(generationConfig.getJavaFormatedName()))
                    .param("resourceType", generationConfig.getProjectSettings().getComponentPath() + "/"
                            + generationConfig.getType() + "/" + generationConfig.getName())
                    .paramArray("adaptables")
                    .param(codeModel.ref("org.apache.sling.api.resource.Resource"))
                    .param(codeModel.ref("org.apache.sling.api.SlingHttpServletRequest"));
        }
        return jDefinedClass;
    }

    /**
     * adds fields to java model.
     *
     * @param properties
     */
    private void _addFieldVars(List<Property> properties, final String propertyType) {
        properties.stream()
                .filter(Objects::nonNull)
                .forEach(property -> _addFieldVar(property, propertyType));
    }

    /**
     * add field variable to to jc.
     *
     * @param property
     */
    private void _addFieldVar(Property property, final String propertyType) {
        if (property != null && StringUtils.isNotBlank(property.getField())) {
            _addPropertyAsPrivateField(property, propertyType);
        }
    }

    /**
     * method that add the fieldname as private to jc.
     *
     * @param property
     * @param propertyType
     */
    private void _addPropertyAsPrivateField(Property property, final String propertyType) {//(String fieldName, String propertyType, String fieldType, final String propertyType) {
        String fieldType = getFieldType(property.getType());
        if (jc.isClass()) {
            JFieldVar jFieldVar = jc.field(PRIVATE, codeModel.ref(fieldType), property.getField());

            if (StringUtils.equalsIgnoreCase(property.getType(), "image")) {
                jFieldVar.annotate(codeModel.ref(ChildRequest.class))
                        .param("injectionStrategy",
                                codeModel.ref(InjectionStrategy.class).staticRef("OPTIONAL"));

            } else if (StringUtils.equalsIgnoreCase(propertyType, Constants.PROPERTY_TYPE_PRIVATE)) {
                jFieldVar.annotate(codeModel.ref(ValueMapValue.class))
                        .param("injectionStrategy",
                                codeModel.ref(InjectionStrategy.class).staticRef("OPTIONAL"));
            } else {
                jFieldVar.annotate(codeModel.ref(SharedValueMapValue.class))
                        .param("injectionStrategy",
                                codeModel.ref(InjectionStrategy.class).staticRef("OPTIONAL"));
            }
        } else if (jc.isInterface()) {
            jc.field(NONE, codeModel.ref(fieldType), property.getField());
        }
    }

    /**
     * adds getters to all the fields available in the java class.
     */
    private void _addGetters() {
        Map<String, JFieldVar> fieldVars = jc.fields();
        if (fieldVars.size() > 0) {
            for (Map.Entry<String, JFieldVar> entry : fieldVars.entrySet()) {
                if (entry.getValue() != null) {
                    _addGetter(entry.getValue());
                }
            }
        }
    }

    /**
     * add getter method for jFieldVar passed in.
     *
     * @param jFieldVar
     */
    private void _addGetter(JFieldVar jFieldVar) {
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
     * @param type
     * @return String returns relevant java type of string passed in.
     */
    private String getFieldType(String type) {
        if (StringUtils.isNotBlank(type)) {
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
            }
        }
        return type;
    }

    /**
     * method just adds getters based on the properties of generationConfig
     *
     * @param properties
     */
    private void _addGettersWithoutFields(List<Property> properties) {
        if (properties != null && properties.size() > 0) {
            properties.forEach(property -> jc.method(NONE, codeModel.ref(getFieldType(property.getType())),
                    Constants.STRING_GET + property.getFieldGetterName()));
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

}
