package com.adobe.aem.compgenerator.models;

import org.apache.commons.lang3.StringUtils;

public class OptionTemplateTxt {
    public enum TemplateType {
        TEMPLATE_COPYRIGHT_JAVA("template-copyright.txt"), TEMPLATE_COPYRIGHT_TEXT("template-copyright-text.txt"),
        TEMPLATE_COPYRIGHT_CSS("template-copyright-css.txt"), TEMPLATE_COPYRIGHT_XML("template-copyright-xml.txt"),
        TEMPLATE_COPYRIGHT_HTL("template-htl.txt"), EMPTY("");

        private final String templateType;

        TemplateType(String templateType) {
            this.templateType = templateType;
        }

        public static TemplateType valueForType(String type) {
            if (StringUtils.isBlank(type)) {
                return EMPTY;
            }
            for (TemplateType value : values()) {
                if (value.getTemplateType().equals(type)) {
                    return value;
                }
            }
            return EMPTY;
        }

        public String getTemplateType() {
            return templateType;
        }

        public String toString() {
            return templateType;
        }
    }

    private final String path;
    private final String templateType;
    private final String templateEnding;

    /**
     * @param path used for creating type and name ending e.g. ("template-htl.selector.html.txt" > template-htl | selector.html )
     */
    public OptionTemplateTxt(String path) {
        this.path = path;
        String basename = StringUtils.substringAfterLast(this.path, "/");
        this.templateType = StringUtils.substringBefore(basename, ".") + StringUtils.substringAfterLast(basename, ".");
        String templateWithoutType = StringUtils.substringAfter(basename, ".");
        this.templateEnding = StringUtils.substringBeforeLast(templateWithoutType, ".");
    }

    public String getPath() {
        return path;
    }

    public String getTemplateType() {
        return templateType;
    }

    public String getTemplateEnding() {
        return templateEnding;
    }
}
