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

import com.adobe.aem.compgenerator.Constants;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.adobe.aem.compgenerator.models.Property;
import com.adobe.aem.compgenerator.utils.CommonUtils;
import com.sun.codemodel.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Root of the code.
 * <p>
 * <p>
 * Here's your JavaCodeModel application.
 *
 * <pre>
 * JavaCodeModel jcm = new JavaCodeModel();
 *
 * // generate source code and write them from jcm.
 * jcm.buildSlingModel(generationConfig);
 * ...
 * </pre>
 * <p>
 * JavaCodeModel creates source code of your sling-model interface and implementation
 * using user data config configuration object.
 */
public class JavaCodeModel {
    private static final Logger LOG = LogManager.getLogger(JavaCodeModel.class);

    private final JCodeModel codeModel;
    private final JCodeModel codeModelTest;

    private GenerationConfig generationConfig;
    private JDefinedClass jc;
    private JDefinedClass jcImpl;

    private List<Property> globalProperties;
    private List<Property> sharedProperties;
    private List<Property> privateProperties;

    public JavaCodeModel() {
        this.codeModel = new JCodeModel();
        this.codeModelTest = new JCodeModel();
    }

    /**
     * Builds your slingModel interface and implementation class with all required
     * sling annotation, fields and getters based on the <code>generationConfig</code>.
     *
     * @param generationConfig the configuration for generating the java code
     */
    public void buildSlingModel(GenerationConfig generationConfig) {
        try {
            this.generationConfig = generationConfig;
            buildInterface();
            buildImplClass();
            if (generationConfig.getOptions().isHasTestClass()) {
                buildTestClass();
            }
            generateCodeFiles();
            String withTestsStr = generationConfig.getOptions().isHasTestClass() ? "with test class " : StringUtils.EMPTY;
            LOG.info("--------------* Sling Model {}successfully generated *--------------", withTestsStr);


        } catch (JClassAlreadyExistsException | IOException e) {
            LOG.error("Failed to create sling model.", e);
        }
    }

    /**
     * Builds your slingModel test class with all required Test annotation,
     * method stubs based on the <code>generationConfig</code>.
     */
    private void buildTestClass() throws JClassAlreadyExistsException {
        JPackage slingModelImplPackage = codeModel._package(jcImpl._package().name());
        for (Iterator<JDefinedClass> it = slingModelImplPackage.classes(); it.hasNext(); ) {
            JDefinedClass slingModelImplClass = it.next();
            TestClassBuilder builder = new TestClassBuilder(codeModelTest, generationConfig, slingModelImplClass.name() + "Test", slingModelImplClass);
            builder.build();
        }
    }

    /**
     * Builds your slingModel interface with all required annotation,
     * fields and getters based on the <code>generationConfig</code>.
     */
    private void buildInterface() {
        InterfaceBuilder builder = new InterfaceBuilder(codeModel, generationConfig, generationConfig.getJavaFormatedName());
        jc = builder.build();
    }

    /**
     * Builds your slingModel implementation with all required sling annotation,
     * fields and getters based on the <code>generationConfig</code>.
     */
    private void buildImplClass() throws JClassAlreadyExistsException {
        ImplementationBuilder builder = new ImplementationBuilder(codeModel, generationConfig, generationConfig.getJavaFormatedName() + "Impl", jc);
        jcImpl = builder.build(CommonUtils.getResourceType(generationConfig));
    }

    /**
     * Generates the slingModel file based on values from the config and the current codeModel object.
     */
    private void generateCodeFiles() throws IOException {
        // RenameFileCodeWritern to rename existing files
        CodeWriter codeWriter = new RenameFileCodeWriter(new File(generationConfig.getProjectSettings().getBundlePath()));

        // PrologCodeWriter to prepend the copyright template in each file
        String templateString = CommonUtils.getTemplateFileAsString(Constants.TEMPLATE_COPYRIGHT_JAVA, generationConfig);
        PrologCodeWriter prologCodeWriter = new PrologCodeWriter(codeWriter, templateString);

        codeModel.build(prologCodeWriter);
        if (generationConfig.getOptions().isHasTestClass()) {
            CodeWriter codeWriterTest = new RenameFileCodeWriter(new File(generationConfig.getProjectSettings().getTestPath()));
            PrologCodeWriter prologCodeWriterTest = new PrologCodeWriter(codeWriterTest, templateString);
            codeModelTest.build(prologCodeWriterTest);
        }

    }

    /**
     * Generates the sling model interface name for a multifield type
     *
     * @param property the property definition for the multifield type
     * @return the sling model interface name
     */
    public static String getMultifieldInterfaceName(Property property) {
        return StringUtils.defaultString(property.getModelName(), CaseUtils.toCamelCase(property.getField(), true) + "Multifield");
    }

    /**
     * Get the java fieldType based on the type input in the generationConfig
     *
     * @param property the property definition
     * @return String returns relevant java type of string passed in.
     */
    public static String getFieldType(Property property) {
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
            return "com.adobe.cq.wcm.core.components.models.Image";
        } else if (type.equalsIgnoreCase("multifield")) {
            return "java.util.List";
        }
        return type;
    }
}
