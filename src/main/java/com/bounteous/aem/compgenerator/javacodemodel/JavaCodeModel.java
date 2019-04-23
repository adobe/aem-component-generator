package com.bounteous.aem.compgenerator.javacodemodel;

import com.bounteous.aem.compgenerator.Constants;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.bounteous.aem.compgenerator.models.Property;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sun.codemodel.JMod.NONE;
import static com.sun.codemodel.JMod.PRIVATE;

public class JavaCodeModel {
    private JCodeModel codeModel;
    private JDefinedClass jc;
    GenerationConfig generationConfig;

    public JavaCodeModel(GenerationConfig generationConfig) {
        this.codeModel = new JCodeModel();
        this.generationConfig = generationConfig;
    }

    public void _createSlingModel() {
        _createSlingInterface();
        _createSlingImpl();
    }

    public void _createSlingInterface() {
        try {
            JPackage jPackage = codeModel._package(Constants.PACKAGE_MODELS);
            jc = jPackage._interface(generationConfig.getJavaFormatedName());
            jc.annotate(codeModel.ref("aQute.bnd.annotation.ConsumerType"));

            if (generationConfig.getOptions().getProperties() != null) {
                _addGettersWithoutFields(generationConfig.getOptions().getProperties());
            }

            codeModel.build(new File(Constants.BUNDLE_LOCATION));

        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void _createSlingImpl() {
        try {
            JPackage jPackage = codeModel._package(Constants.PACKAGE_IMPL);
            JDefinedClass jcInterface = jc;
            jc = jPackage._class(generationConfig.getJavaFormatedName() + "Impl")
                    ._implements(codeModel.ref(jcInterface.fullName()));
            jc = _addSlingAnnotations(jc, jcInterface);

            if (generationConfig.getOptions().getProperties() != null) {
                _addFieldVars(generationConfig.getOptions().getProperties());
                _addGetters();
            }

            codeModel.build(new File(Constants.BUNDLE_LOCATION));
            System.out.println("SlingModel successful " + Constants.BUNDLE_LOCATION);
        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JDefinedClass _addSlingAnnotations(JDefinedClass jDefinedClass, JDefinedClass jcInterface) {
        if (jDefinedClass != null) {
            jDefinedClass.annotate(codeModel.ref("org.apache.sling.models.annotations.Model"))
                    .param("adapters", codeModel.ref(jcInterface.fullName()))
                    .param("resourceType", "hs2-aem-base/components/" + generationConfig.getType() + "/" + generationConfig.getName())
                    .paramArray("adaptables")
                    .param(codeModel.ref("org.apache.sling.api.resource.Resource"))
                    .param(codeModel.ref("org.apache.sling.api.SlingHttpServletRequest"));


        }
        return jDefinedClass;
    }

    private void _addFieldVars(List<Property> properties) {
        properties.stream()
                .filter(Objects::nonNull)
                .forEach(property -> _addFieldVar(property));
    }

    private void _addFieldVar(Property property) {
        if (property != null && StringUtils.isNotBlank(property.getField())) {
            _addPrivateField(property.getField(), getFieldType(property.getType()));
        }
    }

    private void _addPrivateField(String fieldName, String fieldType) {
        if (jc.isClass()) {
            jc.field(PRIVATE, codeModel.ref(fieldType), fieldName)
                    .annotate(codeModel.ref("org.apache.sling.models.annotations.injectorspecific.ValueMapValue"))
                    .param("injectionStrategy",
                            codeModel.ref("org.apache.sling.models.annotations.injectorspecific.InjectionStrategy").staticRef("DEFAULT"));
        } else if (jc.isInterface()) {
            jc.field(NONE, codeModel.ref(fieldType), fieldName);
        }
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
        if (jc.isClass()) {
            JMethod getVar = jc.method(JMod.PUBLIC, jFieldVar.type(), getMethodFormattedString(jFieldVar.name()));
            getVar.body()._return(jFieldVar);
        } else {
            jc.method(NONE, jFieldVar.type(), getMethodFormattedString(jFieldVar.name()));
        }
    }

    private String getFieldType(String type) {
        if (StringUtils.isNotBlank(type)) {
            if (type.equalsIgnoreCase("string") || type.equalsIgnoreCase("text")) {
                return "java.lang.String";
            } else if (type.equalsIgnoreCase("number")) {
                return "java.lang.Integer";
            }
        }
        return type;
    }

    private void _addGettersWithoutFields(List<Property> properties) {
        if (properties != null && properties.size() > 0) {
            properties.forEach(property -> jc.method(NONE, codeModel.ref(getFieldType(property.getType())),
                    "get" + property.getFieldGetterName()));
        }
    }

    private String getMethodFormattedString(String fieldVariable) {
        if (StringUtils.isNotBlank(fieldVariable) && StringUtils.length(fieldVariable) > 0) {
            return "get" + Character.toTitleCase(fieldVariable.charAt(0)) + fieldVariable.substring(1);
        }
        return fieldVariable;
    }
}
