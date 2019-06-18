package com.bounteous.aem.compgenerator.javacodemodel;

import com.adobe.acs.commons.models.injectors.annotation.SharedValueMapValue;
import com.bounteous.aem.compgenerator.Constants;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.bounteous.aem.compgenerator.models.Property;
import com.bounteous.aem.compgenerator.utils.CommonUtils;
import com.hs2solutions.aem.base.core.models.annotations.injectorspecific.ChildRequest;
import com.sun.codemodel.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sun.codemodel.JMod.NONE;
import static com.sun.codemodel.JMod.PRIVATE;
import static com.bounteous.aem.compgenerator.javacodemodel.JavaCodeModel.getFieldType;

public class ImplementationBuilder extends JavaCodeBuilder {
    private static final Logger LOG = LogManager.getLogger(ImplementationBuilder.class);
    private static final String INJECTION_STRATEGY = "injectionStrategy";
    private static final String OPTIONAL_INJECTION_STRATEGY = "OPTIONAL";

    private final String className;
    private final JClass interfaceClass;
    private final JPackage implPackage;

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
    }

    public void build(String resourceType) throws JClassAlreadyExistsException {
        JDefinedClass jc = this.implPackage._class(this.className)._implements(this.interfaceClass);
        addSlingAnnotations(jc, this.interfaceClass, resourceType);

        addFieldVars(jc, globalProperties, Constants.PROPERTY_TYPE_GLOBAL);
        addFieldVars(jc, sharedProperties, Constants.PROPERTY_TYPE_SHARED);
        addFieldVars(jc, privateProperties, Constants.PROPERTY_TYPE_PRIVATE);

        addGetters(jc);
    }

    private void addSlingAnnotations(JDefinedClass jDefinedClass, JClass adapterClass, String resourceType) {
        JAnnotationUse jAUse = jDefinedClass.annotate(codeModel.ref(Model.class));
        jAUse.param("adapters", adapterClass)
                .paramArray("adaptables")
                .param(codeModel.ref(Resource.class))
                .param(codeModel.ref(SlingHttpServletRequest.class));
        if (StringUtils.isNotBlank(resourceType)) {
            jAUse.param("resourceType", resourceType);
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
        String fieldType = getFieldType(property);

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

    }

    /**
     * method that add the fieldname as private and adds a class to jc
     */
    private void addPropertyAndObjectAsPrivateField(JDefinedClass jc, Property property) {
        String modelClassName = StringUtils.defaultString(property.getModelName(), CaseUtils.toCamelCase(property.getField(), true));

        // Create the multifield item
        if (!property.getUseExistingModel()) {
            buildImplementation(property.getItems(), modelClassName);
        }

        String fieldType = getFieldType(property);
        JClass narrowedClass = codeModel.ref(generationConfig.getProjectSettings().getModelInterfacePackage() + "." + modelClassName);
        JClass fieldClass = codeModel.ref(fieldType).narrow(narrowedClass);
        JFieldVar jFieldVar = jc.field(PRIVATE, fieldClass, property.getField());
        jFieldVar.annotate(codeModel.ref(ChildRequest.class))
                .param(INJECTION_STRATEGY,
                        codeModel.ref(InjectionStrategy.class).staticRef(OPTIONAL_INJECTION_STRATEGY));
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
        getMethod.body()._return(jFieldVar);
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
        } catch (JClassAlreadyExistsException ex) {
            LOG.error("Failed to generate child implementation classes.", ex);
        }
    }
}
