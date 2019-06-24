package com.bounteous.aem.compgenerator.javacodemodel;

import com.adobe.cq.export.json.ComponentExporter;
import com.bounteous.aem.compgenerator.Constants;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.bounteous.aem.compgenerator.models.Property;
import com.bounteous.aem.compgenerator.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.codemodel.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

import static com.sun.codemodel.JMod.NONE;
import static com.bounteous.aem.compgenerator.javacodemodel.JavaCodeModel.getFieldType;

/**
 * <p>
 * Manages generating the necessary details to create the sling model interface.
 * </p>
 */
public class InterfaceBuilder extends JavaCodeBuilder {
    private static final Logger LOG = LogManager.getLogger(InterfaceBuilder.class);

    private String interfaceClassName;

    /**
     * Construct a interface class builder
     *
     * @param codeModel        The {@link JCodeModel codeModel}
     * @param generationConfig The {@link GenerationConfig generationConfig}
     * @param interfaceName    The name of the interface
     */
    public InterfaceBuilder(JCodeModel codeModel, GenerationConfig generationConfig, String interfaceName) {
        super(codeModel, generationConfig);
        this.interfaceClassName = interfaceName;
    }

    /**
     * Builds the interface class based on the configuration file.
     *
     * @return reference to the Interface
     */
    public JDefinedClass build() {
        String comment = "Defines the {@code "
                + generationConfig.getJavaFormatedName()
                + "} Sling Model used for the {@code "
                + CommonUtils.getResourceType(generationConfig)
                + "} component.";

        return buildInterface(this.interfaceClassName, comment, globalProperties, sharedProperties, privateProperties);
    }

    /**
     * method just adds getters based on the properties of generationConfig
     *
     * @param jc         the interface class
     * @param properties the list of properties
     */
    private void addGettersWithoutFields(JDefinedClass jc, List<Property> properties) {
        if (properties != null && !properties.isEmpty()) {
            properties.stream()
                    .filter(Objects::nonNull)
                    .forEach(property -> {
                        JMethod method = jc.method(NONE, getGetterMethodReturnType(property), Constants.STRING_GET + property.getFieldGetterName());
                        addJavadocToMethod(method, property);
                        if (!property.isShouldExporterExpose()) {
                            method.annotate(codeModel.ref(JsonIgnore.class));
                        }
                        if (property.getType().equalsIgnoreCase("multifield")
                                && property.getItems().size() > 1) {
                            buildMultifieldInterface(property);
                        }
                    });
        }
    }

    private void buildMultifieldInterface(Property property) {
        if (!property.getUseExistingModel()) {
            String modelInterfaceName = JavaCodeModel.getMultifieldInterfaceName(property);
            String childComment = "Defines the {@code "
                    + modelInterfaceName
                    + "} Sling Model used for the multifield in {@code "
                    + CommonUtils.getResourceType(generationConfig)
                    + "} component.";

            buildInterface(modelInterfaceName, childComment, property.getItems());
        }
    }

    @SafeVarargs
    private final JDefinedClass buildInterface(String interfaceName, String comment, List<Property>... propertiesLists) {
        try {
            JPackage jPackage = codeModel._package(generationConfig.getProjectSettings().getModelInterfacePackage());
            JDefinedClass interfaceClass = jPackage._interface(interfaceName);
            interfaceClass.javadoc().append(comment);
            interfaceClass.annotate(codeModel.ref("aQute.bnd.annotation.ConsumerType"));

            if (generationConfig.getOptions().isAllowExporting()) {
                interfaceClass._extends(codeModel.ref(ComponentExporter.class));
            }
            if (propertiesLists != null) {
                for (List<Property> properties : propertiesLists) {
                    addGettersWithoutFields(interfaceClass, properties);
                }
            }
            return interfaceClass;
        } catch (JClassAlreadyExistsException e) {
            LOG.error("Failed to generate child interface.", e);
        }

        return null;
    }

    /**
     * Gets the return type of the getter method based on what type of property it is referring to.
     *
     * @param property the property for which the return type is calculated.
     * @return the type being returned by the getter
     */
    private JType getGetterMethodReturnType(final Property property) {
        String fieldType = getFieldType(property);
        if (property.getType().equalsIgnoreCase("multifield")) {
            if (property.getItems().size() == 1) {
                return codeModel.ref(fieldType).narrow(codeModel.ref(getFieldType(property.getItems().get(0))));
            } else {
                String narrowedClassName = StringUtils.defaultString(property.getModelName(),
                        CaseUtils.toCamelCase(property.getField(), true) + "Multifield");
                return codeModel.ref(fieldType).narrow(codeModel.ref(narrowedClassName));
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
}
