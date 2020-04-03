package com.adobe.aem.compgenerator.javacodemodel;

import com.adobe.aem.compgenerator.Constants;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.sun.codemodel.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

/**
 * The type Test class builder.
 */
public class TestClassBuilder extends JavaCodeBuilder  {

    private final String className;
    private final JDefinedClass implementationClass;
    private final JPackage testPackage;

    /**
     * Instantiates a new Test class builder.
     *
     * @param codeModelTest       the code model
     * @param generationConfig    the generation config
     * @param className           the class name
     * @param implementationClass the implementation class
     */
    protected TestClassBuilder(JCodeModel codeModelTest, GenerationConfig generationConfig, String className,
                               JDefinedClass implementationClass) {
        super(codeModelTest, generationConfig);
        this.className = className;
        this.implementationClass = implementationClass;
        this.testPackage = codeModelTest._package(generationConfig.getProjectSettings().getModelImplPackage());
    }

    /**
     * Build.
     *
     * @throws JClassAlreadyExistsException the j class already exists exception
     */
    public void build() throws JClassAlreadyExistsException {
        JDefinedClass jc = this.testPackage._class(this.className);
        addSetupMethod(jc);
        addTestMethods(jc, implementationClass);
    }

    private void addSetupMethod(JDefinedClass jc) {
        JMethod setUpMethod = jc.method(JMod.PUBLIC, jc.owner().VOID, "setUp")
                ._throws(Exception.class);
        if (generationConfig.getOptions().getJunitVersion() == Constants.JUNIT_VERSION_4) {
            setUpMethod.annotate(codeModel.ref(Before.class));
        } else {
            setUpMethod.annotate(codeModel.ref(BeforeEach.class));
        }
        JBlock block = setUpMethod.body();
        block.directStatement("// TODO: Test Setup");
    }

    private void addTestMethods(JDefinedClass jc, JDefinedClass implementationClass) {
        Map<String, JFieldVar> fieldVars = implementationClass.fields();
        if (!fieldVars.isEmpty()) {
            for (Map.Entry<String, JFieldVar> entry : fieldVars.entrySet()) {
                if (entry.getValue() != null) {
                    if (generationConfig.getOptions().getJunitVersion() == Constants.JUNIT_VERSION_4) {
                        addTestMethod(jc, entry.getValue(), Test.class, Assert.class);
                    } else {
                        addTestMethod(jc, entry.getValue(), org.junit.jupiter.api.Test.class, org.junit.jupiter.api.Assertions.class);
                    }
                }
            }
        }
    }

    private void addTestMethod(JDefinedClass jc, JFieldVar jFieldVar, Class<?> testClassAnnotation,
                               Class<?> assertClassAnnotation) {
        JMethod getMethod = jc.method(JMod.PUBLIC, jc.owner().VOID, getMethodFormattedString(jFieldVar.name()));
        getMethod.annotate(codeModel.ref(testClassAnnotation));
        JBlock block = getMethod.body();
        JClass assertClassRef = codeModel.ref(assertClassAnnotation);
        JInvocation assertNotYetImplemented = assertClassRef.staticInvoke("fail").arg("Not Yet Implemented");
        block.add(assertNotYetImplemented);
    }

    private String getMethodFormattedString(String fieldVariable) {
        if (StringUtils.isNotBlank(fieldVariable) && StringUtils.length(fieldVariable) > 0) {
            return Constants.STRING_TEST + Character.toTitleCase(fieldVariable.charAt(0)) + fieldVariable.substring(1);
        }
        return fieldVariable;
    }
}
