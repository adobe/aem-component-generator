package com.adobe.aem.compgenerator.javacodemodel;

import com.adobe.aem.compgenerator.Constants;
import com.adobe.aem.compgenerator.exceptions.GeneratorException;
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
public class TestClassBuilder extends JavaCodeBuilder {

    private static final String JUNIT_UNSUPPORTED_VERSION_EXCEPTION = "Component generator either supports Junit 5 or Junit 4.";

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
        int junitVersion = generationConfig.getOptions().getJunitVersion();
        if (junitVersion == Constants.JUNIT_VERSION_5) {
            setUpMethod.annotate(codeModel.ref(BeforeEach.class));
        } else if ((junitVersion == Constants.JUNIT_VERSION_4)) {
            setUpMethod.annotate(codeModel.ref(Before.class));
        } else {
            throw new GeneratorException(JUNIT_UNSUPPORTED_VERSION_EXCEPTION);
        }
        JBlock block = setUpMethod.body();
        block.directStatement("// TODO: Test Setup");
    }

    private void addTestMethods(JDefinedClass jc, JDefinedClass implementationClass) {
        Map<String, JFieldVar> fieldVars = implementationClass.fields();
        if (!fieldVars.isEmpty()) {
            for (Map.Entry<String, JFieldVar> entry : fieldVars.entrySet()) {
                if (entry.getValue() != null) {
                    int junitVersion = generationConfig.getOptions().getJunitVersion();
                    if (junitVersion == Constants.JUNIT_VERSION_5) {
                        addTestMethod(jc, entry.getValue(), org.junit.jupiter.api.Test.class, org.junit.jupiter.api.Assertions.class);
                    } else if (junitVersion == Constants.JUNIT_VERSION_4) {
                        addTestMethod(jc, entry.getValue(), Test.class, Assert.class);
                    } else {
                        throw new GeneratorException(JUNIT_UNSUPPORTED_VERSION_EXCEPTION);
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
            return Constants.STRING_TEST
                    + StringUtils.capitalize(Constants.STRING_GET)
                    + Character.toTitleCase(fieldVariable.charAt(0)) + fieldVariable.substring(1);
        }
        return fieldVariable;
    }
}
