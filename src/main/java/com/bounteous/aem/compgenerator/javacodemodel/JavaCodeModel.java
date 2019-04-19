package com.bounteous.aem.compgenerator.javacodemodel;

import com.bounteous.aem.compgenerator.Constants;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.bounteous.aem.compgenerator.models.Property;
import com.bounteous.aem.compgenerator.utils.ComponentGeneratorUtils;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sun.codemodel.JMod.PRIVATE;

public class JavaCodeModel {
    private JCodeModel codeModel;
    private JPackage jPackage;
    private JDefinedClass jc;
    GenerationConfig generationConfig;

    public JavaCodeModel(GenerationConfig generationConfig) {
        this.codeModel = new JCodeModel();
        this.jPackage = codeModel._package(Constants.PACKAGE_NAME);
        this.generationConfig = generationConfig;
    }

    public void _createSlingModel() {
        try {
            jc = jPackage._class(CaseUtils.toCamelCase(generationConfig.getName().replaceAll("[^a-z0-9+]", " "), true));
            jc = _addSlingAnnotations(jc);

            _addFieldVars(generationConfig.getOptions().getProperties());
            _addGetters();

            codeModel.build(new File(Constants.BUNDLE_LOCATION));
            System.out.println("SlingModel successful "+ Constants.BUNDLE_LOCATION);

        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JDefinedClass _addSlingAnnotations(JDefinedClass jDefinedClass) {
        if (jDefinedClass != null) {
            jDefinedClass.annotate(codeModel.ref("org.apache.sling.models.annotations.Model"))
                    .param("adaptables", codeModel.ref("org.apache.sling.api.resource.Resource"));
        }
        return jDefinedClass;
    }

    private void _addFieldVars(List<Property> properties) {
        properties.stream()
                .filter(Objects::nonNull)
                .forEach(property -> _addField(property));
    }

    private void _addField(Property property) {
        if (property != null && StringUtils.isNotBlank(property.getField())) {
            if (property.getType().equalsIgnoreCase("string") || property.getType().equalsIgnoreCase("text")) {
                _addPrivateField(property.getField(), "java.lang.String");
            }
        }
    }

    private void _addPrivateField(String fieldName, String fieldType) {
        JFieldVar field = jc.field(PRIVATE, codeModel.ref(fieldType), fieldName);
        field.annotate(codeModel.ref("org.apache.sling.models.annotations.injectorspecific.ValueMapValue"))
                .param("injectionStrategy", codeModel.ref("org.apache.sling.models.annotations.injectorspecific.InjectionStrategy")
                        .staticRef("OPTIONAL"));
    }

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

    private void _addGetter(JFieldVar jFieldVar) {
        JMethod getVar = jc.method(JMod.PUBLIC, jFieldVar.type(), "get" + Character.toTitleCase(jFieldVar.name().charAt(0)) + jFieldVar.name().substring(1));
        getVar.body()._return(jFieldVar);
    }
}
