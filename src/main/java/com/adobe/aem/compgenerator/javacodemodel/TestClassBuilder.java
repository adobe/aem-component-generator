package com.adobe.aem.compgenerator.javacodemodel;

import com.adobe.aem.compgenerator.Constants;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.sun.codemodel.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        setUpMethod.annotate(codeModel.ref(Before.class));
        JBlock block = setUpMethod.body();
        block.directStatement("// TODO: Test Setup");
    }

    private void addTestMethods(JDefinedClass jc, JDefinedClass implementationClass) {
        Map<String, JFieldVar> fieldVars = implementationClass.fields();
        if (!fieldVars.isEmpty()) {
            for (Map.Entry<String, JFieldVar> entry : fieldVars.entrySet()) {
                if (entry.getValue() != null) {
                    addTestMethod(jc, entry.getValue());
                }
            }
        }
    }

    private void addTestMethod(JDefinedClass jc, JFieldVar jFieldVar) {
        JMethod getMethod = jc.method(JMod.PUBLIC, jc.owner().VOID, getMethodFormattedString(jFieldVar.name()));
        getMethod.annotate(codeModel.ref(Test.class));
        JBlock block = getMethod.body();
        JClass assertClassRef = codeModel.ref(Assert.class);
        JInvocation assertNotYetImplemented = assertClassRef.staticInvoke("fail").arg("Not Yet Implemented");
        block.add(assertNotYetImplemented);
    }

    private String getMethodFormattedString(String fieldVariable) {
        if (StringUtils.isNotBlank(fieldVariable) && StringUtils.length(fieldVariable) > 0) {
            return Constants.STRING_GET + Character.toTitleCase(fieldVariable.charAt(0)) + fieldVariable.substring(1);
        }
        return fieldVariable;
    }
}
