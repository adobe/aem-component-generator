/*
 * ***********************************************************************
 * BOUNTEOUS CONFIDENTIAL
 * ___________________
 *
 * Copyright 2019 Bounteous
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of Bounteous and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Bounteous
 * and its suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Bounteous.
 * ***********************************************************************
 */
package com.bounteous.aem.compgenerator.utils;

import com.bounteous.aem.compgenerator.Constants;
import com.bounteous.aem.compgenerator.exceptions.GeneratorException;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.bounteous.aem.compgenerator.models.Property;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;
import java.util.Objects;

public class DialogUtils {
    private static final Logger LOG = LogManager.getLogger(DialogUtils.class);

    /**
     * creates dialog xml by adding the properties in data-config json file.
     *
     * @param dialogType dialogType to dialog xml structure.
     */
    public static void createDialogXml(final GenerationConfig generationConfig, final String dialogType) {
        String dialogPath = generationConfig.getCompDir() + "/" + dialogType;
        try {
            CommonUtils.createFolder(dialogPath);

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element rootElement = createDialogRoot(doc, generationConfig, dialogType);

            List<Property> properties = generationConfig.getOptions().getProperties();
            if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_GLOBAL)) {
                properties = generationConfig.getOptions().getGlobalProperties();
            } else if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_SHARED)) {
                properties = generationConfig.getOptions().getSharedProperties();
            }

            if (properties != null && properties.size() > 0) {
                Node currentNode = updateDefaultNodeStructure(doc, rootElement, dialogType);

                properties.stream().filter(Objects::nonNull)
                        .map(property -> createPropertyNode(doc, currentNode, property)).filter(Objects::nonNull)
                        .forEach(a -> currentNode.appendChild(a));
            }
            doc.appendChild(rootElement);
            XMLUtils.transformDomToFile(doc, dialogPath + "/" + Constants.FILENAME_CONTENT_XML);
            LOG.info("Created : " + dialogPath + "/" + Constants.FILENAME_CONTENT_XML);
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating Dialog xml : " + dialogPath);
        }
    }

    /**
     * Generates the root elements of what will be the _cq_dialog/.content.xml
     * 
     * @param document
     * @param generationConfig
     * @param dialogType
     * @return
     */
    private static Element createDialogRoot(Document document, GenerationConfig generationConfig, String dialogType) {
        Element rootElement = XMLUtils.createRootElement(document);
        rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        rootElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_DIALOG);
        if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_GLOBAL)) {
            rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE,
                    generationConfig.getTitle() + " (Global Properties)");
        } else if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_SHARED)) {
            rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE,
                    generationConfig.getTitle() + " (Shared Properties)");
        } else {
            rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE, generationConfig.getTitle());
        }
        return rootElement;
    }

    /**
     * adds a dialog property xml node with all input attr under the document.
     *
     * @param document
     * @param property project object contains attributes.
     * @return
     */
    private static Element createPropertyNode(Document document, Node currentNode, Property property) {

        Element propertyNode = document.createElement(property.getField());

        propertyNode.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        propertyNode.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, getSlingResourceType(property.getType()));

        // Some of the properties are optional based on the different types available.
        if (StringUtils.isNotEmpty(property.getLabel())) {
            propertyNode.setAttribute(Constants.PROPERTY_FIELDLABEL, property.getLabel());
        }
        if (StringUtils.isNotEmpty(property.getDescription())) {
            propertyNode.setAttribute(Constants.PROPERTY_FIELDDESC, property.getDescription());
        }
        if (StringUtils.isNotEmpty(property.getField()) && (!property.getType().equalsIgnoreCase("radiogroup"))
                || !property.getType().equalsIgnoreCase("image")) {
            propertyNode.setAttribute(Constants.PROPERTY_NAME, "./" + property.getField());
            propertyNode.setAttribute(Constants.PROPERTY_CQ_MSM_LOCKABLE, "./" + property.getField());
        }

        processAttributes(propertyNode, property);
        if (property.getItems() != null && !property.getItems().isEmpty()) {
            if (!property.getType().equalsIgnoreCase("multifield")) {
                Node items = propertyNode.appendChild(createUnStructuredNode(document, "items"));
                processItems(document, items, property);
            } else {
                Element field = document.createElement("field");
                field.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
                field.setAttribute(Constants.PROPERTY_NAME, "./" + property.getField());
                field.setAttribute(Constants.PROPERTY_CQ_MSM_LOCKABLE, "./" + property.getField());

                if (property.getItems().size() == 1) {
                    Property prop = property.getItems().get(0);
                    field.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, getSlingResourceType(prop.getType()));
                    processAttributes(field, prop);
                    propertyNode.appendChild(field);
                } else {
                    propertyNode.setAttribute(Constants.PROPERTY_COMPOSITE, "{Boolean}true");
                    field.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_CONTAINER);
                    Node items = field.appendChild(createUnStructuredNode(document, "items"));
                    processItems(document, items, property);
                }

                propertyNode.appendChild(field);
            }
        }

        if (property.getType().equalsIgnoreCase("image")) {
            addImagePropertyValues(propertyNode, property);
            currentNode.appendChild(propertyNode);

            Element hiddenImageNode = document.createElement(property.getField() + "ResType");
            addImageHiddenProperyValues(hiddenImageNode, property);
            return hiddenImageNode;
        }

        return propertyNode;
    }

    /**
     * Processes the attributes for a propertyNode
     * 
     * @param propertyNode
     * @param property
     */
    private static void processAttributes(Element propertyNode, Property property) {
        if (property.getAttributes() != null && property.getAttributes().size() > 0) {
            property.getAttributes().entrySet().stream()
                    .forEach(entry -> propertyNode.setAttribute(entry.getKey(), entry.getValue()));
        }
    }

    private static void processItems(Document document, Node itemsNode, Property property) {
        for (Property item : property.getItems()) {
            Element optionNode = document.createElement(item.getField());
            optionNode.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
            String resourceType = getSlingResourceType(item.getType());
            if (StringUtils.isNotEmpty(resourceType)) {
                optionNode.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, resourceType);
            }
            
            if (StringUtils.equalsIgnoreCase("multifield", property.getType())) {
                optionNode.setAttribute(Constants.PROPERTY_NAME, "./" + item.getField());
            }

            processAttributes(optionNode, item);
            itemsNode.appendChild(optionNode);
        }
    }

    /**
     * Adds the properties specific to the image node. These could all have been
     * included as attributes in the configuration json file, but they never/rarely
     * change, so hardcoding them here seems safe to do.
     * 
     * @param imageNode
     * @param property
     */
    private static void addImagePropertyValues(Element imageNode, Property property) {
        imageNode.setAttribute(Constants.PROPERTY_NAME, "./" + property.getField() + "/file");
        imageNode.setAttribute(Constants.PROPERTY_CQ_MSM_LOCKABLE, "./" + property.getField() + "/file");
        imageNode.setAttribute("allowUpload", "{Boolean}false");
        imageNode.setAttribute("autoStart", "{Boolean}false");
        imageNode.setAttribute("class", "cq-droptarget");
        imageNode.setAttribute("fileReferenceParameter", "./" + property.getField() + "/fileReference");
        imageNode.setAttribute("mimeTypes", "[image/gif,image/jpeg,image/png,image/webp,image/tiff,image/svg+xml]");
        imageNode.setAttribute("multiple", "{Boolean}false");
        imageNode.setAttribute("title", "Drag to select image");
        imageNode.setAttribute("uploadUrl", "${suffix.path}");
        imageNode.setAttribute("useHTML5", "{Boolean}true");
    }

    /**
     * Adds the properties specific to the hidden image node that allows the image
     * dropzone to operate properly on dialogs.
     * 
     * @param hiddenImageNode
     * @param property
     */
    private static void addImageHiddenProperyValues(Element hiddenImageNode, Property property) {
        hiddenImageNode.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        hiddenImageNode.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_HIDDEN);
        hiddenImageNode.setAttribute("name", "./" + property.getField() + "/" + Constants.PROPERTY_SLING_RESOURCETYPE);
        hiddenImageNode.setAttribute("value", Constants.RESOURCE_TYPE_IMAGE_HIDDEN_TYPE);
    }

    /**
     * builds default node structure of dialog xml in the document passed in based
     * on dialogType.
     *
     * @param document
     * @param root
     * @param dialogType
     * @return
     */
    private static Node updateDefaultNodeStructure(Document document, Element root, String dialogType) {
        Element containerElement = document.createElement("content");
        containerElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        containerElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_CONTAINER);

        Element layoutElement1 = document.createElement("layout");
        layoutElement1.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        layoutElement1.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_FIXEDCOLUMNS);
        layoutElement1.setAttribute("margin", "{Boolean}false");

        Element columnElement = document.createElement("column");
        columnElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        columnElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_CONTAINER);

        Node containerNode = root.appendChild(containerElement);

        containerNode.appendChild(layoutElement1);
        return containerNode.appendChild(createUnStructuredNode(document, "items")).appendChild(columnElement)
                .appendChild(createUnStructuredNode(document, "items"));
    }

    /**
     * Creates a node with the jcr:primaryType set to nt:unstructured
     * 
     * @param document
     * @param nodeName
     * @return
     */
    private static Node createUnStructuredNode(Document document, String nodeName) {
        Element element = document.createElement(nodeName);
        element.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        return element;
    }

    /**
     * Determine the proper sling:resourceType
     * 
     * @param type
     * @return
     */
    private static String getSlingResourceType(String type) {
        if (StringUtils.isNotBlank(type)) {
            if (StringUtils.equalsIgnoreCase("textfield", type)) {
                return Constants.RESOURCE_TYPE_TEXTFIELD;
            } else if (StringUtils.equalsIgnoreCase("numberfield", type)) {
                return Constants.RESOURCE_TYPE_NUMBER;
            } else if (StringUtils.equalsIgnoreCase("checkbox", type)) {
                return Constants.RESOURCE_TYPE_CHECKBOX;
            } else if (StringUtils.equalsIgnoreCase("pathfield", type)) {
                return Constants.RESOURCE_TYPE_PATHFIELD;
            } else if (StringUtils.equalsIgnoreCase("textarea", type)) {
                return Constants.RESOURCE_TYPE_TEXTAREA;
            } else if (StringUtils.equalsIgnoreCase("hidden", type)) {
                return Constants.RESOURCE_TYPE_HIDDEN;
            } else if (StringUtils.equalsIgnoreCase("datepicker", type)) {
                return Constants.RESOURCE_TYPE_DATEPICKER;
            } else if (StringUtils.equalsIgnoreCase("select", type)) {
                return Constants.RESOURCE_TYPE_SELECT;
            } else if (StringUtils.equalsIgnoreCase("radiogroup", type)) {
                return Constants.RESOURCE_TYPE_RADIOGROUP;
            } else if (StringUtils.equalsIgnoreCase("radio", type)) {
                return Constants.RESOURCE_TYPE_RADIO;
            } else if (StringUtils.equalsIgnoreCase("image", type)) {
                return Constants.RESOURCE_TYPE_IMAGE;
            } else if (StringUtils.equalsIgnoreCase("multifield", type)) {
                return Constants.RESOURCE_TYPE_MULTIFIELD;
            }
        }
        return null;
    }
}
