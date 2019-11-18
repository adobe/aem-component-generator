package com.adobe.aem.compgenerator.utils;

import com.adobe.aem.compgenerator.Constants;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.adobe.aem.compgenerator.models.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HTMLUtils {

    public static String renderHtml(GenerationConfig generationConfig) {
        List<Property> props = getAllProperties(generationConfig);
        String slingModel = StringUtils.uncapitalize(generationConfig.getJavaFormatedName()) + "Model";
        StringBuilder renderedHtml = new StringBuilder();

        for (Property prop : props) {
            String label = getLabel(prop);

            if (Constants.TYPE_CHECKBOX.equals(prop.getType())) {
                renderedHtml.append(generateParagraphHtml(label,
                        prop.getField(),
                        slingModel,
                        " ? 'checked' : 'unchecked'"
                ));
            } else if (Constants.TYPE_DATEPICKER.equals(prop.getType())) {
                renderedHtml.append(generateParagraphHtml(label,
                        prop.getField(),
                        slingModel,
                        ".time.toGMTString"
                ));
            } else if (Constants.TYPE_IMAGE.equals(prop.getType())) {
                renderedHtml.append(generateImageHtml(label, prop.getField(), slingModel));
            } else if (Constants.TYPE_MULTIFIELD.equals(prop.getType())) {
                renderedHtml.append(generateListHtml(prop, slingModel));
            } else {
                renderedHtml.append(generateParagraphHtml(label,
                        prop.getField(),
                        slingModel,
                        StringUtils.EMPTY));
            }
        }
        return renderedHtml.toString();
    }

    private static String getLabel(Property prop) {
        if (Constants.TYPE_CHECKBOX.equals(prop.getType())) {
            return prop.getAttributes().get("text");
        }
        if (Constants.TYPE_HIDDEN.equals(prop.getType())) {
            return "Hidden Field (" + prop.getField() + ")";
        }
        String label = prop.getLabel();
        if (label == null) {
            label = prop.getAttributes().get("value");
            if (label == null) {
                label = prop.getField();
            }
        }

        return label;
    }

    private static String generateImageHtml(String label, String field, String slingModel) {
        return "    <p>" +
                label +
                ": <img src=\"${" +
                slingModel +
                "." +
                field +
                ".src}\"/></p>\n";
    }

    private static String generateListHtml(Property prop, String slingModel) {
        String initialListHtml = "    <div data-sly-list=\"${" +
                slingModel +
                "." +
                prop.getField() +
                "}\">\n        <p>";
        if (prop.getItems().size() > 1) {
            StringBuilder items = new StringBuilder(initialListHtml);
            int index = 1;
            for (Property property : prop.getItems()) {
                items.append(property.getLabel())
                        .append(": ${item.")
                        .append(property.getField())
                        .append(prop.getItems().size() == index ? "}" : "} | ");
                index++;
            }
            return items + "</p>\n    </div>\n";
        } else {
            return initialListHtml +
                    prop.getLabel() +
                    ": ${item}</p>\n    </div>\n";
        }
    }

    private static String generateParagraphHtml(String label, String field, String slingModel, String additional) {
        return "    <p>" +
                label +
                ": ${" +
                slingModel +
                "." +
                field +
                additional +
                "}</p>\n";
    }

    private static List<Property> getAllProperties(GenerationConfig generationConfig) {
        List<Property> globalProperties = generationConfig.getOptions().getGlobalProperties();
        List<Property> sharedProperties = generationConfig.getOptions().getSharedProperties();
        List<Property> localProperties = generationConfig.getOptions().getProperties();
        return Stream.of(globalProperties, sharedProperties, localProperties)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
