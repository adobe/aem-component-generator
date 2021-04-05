/*
 * #%L
 * AEM Component Generator
 * %%
 * Copyright (C) 2019 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.aem.compgenerator.utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.adobe.aem.compgenerator.Constants;
import com.adobe.aem.compgenerator.exceptions.GeneratorException;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.adobe.aem.compgenerator.models.Property;
import com.adobe.aem.compgenerator.models.Tab;

public class DialogUtils {

    /**
     * Creates dialog xml by adding the properties in data-config json file.
     *
     * @param generationConfig The {@link GenerationConfig} object with all the
     *            populated values
     * @param dialogType The type of dialog to create (regular, shared or global)
     */
    public static void createDialogXml(final GenerationConfig generationConfig, final String dialogType) {
        String dialogPath = generationConfig.getCompDir() + "/" + dialogType;
        try {
            CommonUtils.createFolder(dialogPath);

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = createDialogRoot(doc, generationConfig, dialogType);

            if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_DIALOG)) {
                createDialogProperties(doc, rootElement, generationConfig.getOptions().getTabProperties(),
                        generationConfig.getOptions().getProperties());
            } else if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_GLOBAL)) {
                createDialogProperties(doc, rootElement, generationConfig.getOptions().getGlobalTabProperties(),
                        generationConfig.getOptions().getGlobalProperties());
            } else if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_SHARED)) {
                createDialogProperties(doc, rootElement, generationConfig.getOptions().getSharedTabProperties(),
                        generationConfig.getOptions().getSharedProperties());
            }

            doc.appendChild(rootElement);
            XMLUtils.transformDomToFile(doc, dialogPath + "/" + Constants.FILENAME_CONTENT_XML);
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating Dialog xml : " + dialogPath, e);
        }
    }

    /**
     * Creates the tab dialog properties.
     *
     * @param doc The {@link Document} object
     * @param rootElement the root element
     * @param tabs The {@link Tab} object
     */
    private static void createDialogProperties(Document doc, Element rootElement, List<Tab> tabs,
            List<Property> properties) {

        if (null != properties && !properties.isEmpty()) {
            if (null != tabs && !tabs.isEmpty()) {
                Element currentNode = createTabsParentNodeStructure(doc, rootElement);

                Map<String, Property> propertiesMap = properties.stream()
                        .collect(Collectors.toMap(Property::getField, Function.identity()));

                for (Tab tab : tabs) {
                    Element tabNode = createTabStructure(doc, tab, currentNode);
                    List<Property> sortedProperties = tab.getFields().stream().map(propertiesMap::get)
                            .collect(Collectors.toList());
                    createNodeStructure(doc, sortedProperties, tabNode);
                }
            } else {
                Element currentNode = updateDefaultNodeStructure(doc, rootElement);
                createNodeStructure(doc, properties, currentNode);
            }
        }

    }

    /**
     * Creates the node structure.
     *
     * @param doc The {@link Document} object
     * @param properties {@link Property}
     * @param currentNode the current node
     */
    private static void createNodeStructure(Document doc, List<Property> properties, Element currentNode) {
        properties.stream().filter(Objects::nonNull)
                .forEach(property -> createPropertyNode(doc, currentNode, property));
    }

    /**
     * Generates the root elements of what will be the _cq_dialog/.content.xml.
     *
     * @param document The {@link Document} object
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     * @param dialogType The type of dialog to create (regular, shared or global)
     * @return Element
     */
    protected static Element createDialogRoot(Document document, GenerationConfig generationConfig, String dialogType) {
        Element rootElement = XMLUtils.createRootElement(document, generationConfig);

        rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        rootElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_DIALOG);

        if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_GLOBAL)) {
            rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE,
                    generationConfig.getTitle() + " (Global Properties)");
        } else if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_SHARED)) {
            rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE,
                    generationConfig.getTitle() + " (Shared Properties)");
        } else if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_DESIGN_DIALOG)) {
            rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE, generationConfig.getTitle() + " Design Dialog");
        } else {
            rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE, generationConfig.getTitle());
        }

        return rootElement;
    }

    /**
     * Adds a dialog property xml node with all input attr under the document.
     *
     * @param document The {@link Document} object
     * @param currentNode the current {@link Element} object
     * @param property The {@link Property} object contains attributes
     * @return Element
     */
    private static void createPropertyNode(Document document, final Element currentNode, Property property) {
        Element propertyNode = document.createElement(property.getField());

        propertyNode.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        Optional.ofNullable(getSlingResourceType(property.getType())).ifPresent(resType -> {
            propertyNode.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, resType);
        });

        // Some of the properties are optional based on the different types available.
        addBasicProperties(propertyNode, property);

        getPrimaryFieldName(property).ifPresent(name -> {
            propertyNode.setAttribute(Constants.PROPERTY_NAME, name);
            propertyNode.setAttribute(Constants.PROPERTY_CQ_MSM_LOCKABLE, name);
        });

        if ("image".equalsIgnoreCase(property.getType())) {
            addImageHiddenProperyValues(document, currentNode, property);
            // add these default image attributes BEFORE generic attribute handling, to allow for
            // individual overrides
            addImagePropertyValues(propertyNode, property);
        }

        processAttributes(propertyNode, property);

        if (property.getItems() != null && !property.getItems().isEmpty()) {
            if (!"multifield".equalsIgnoreCase(property.getType())) {
                Element items = createUnStructuredNode(document, "items");
                propertyNode.appendChild(items);
                processItems(document, items, property);
            } else {
                Element field = document.createElement("field");
                field.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);

                field.setAttribute(Constants.PROPERTY_NAME, "./" + property.getField());
                field.setAttribute(Constants.PROPERTY_CQ_MSM_LOCKABLE, "./" + property.getField());

                field.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_FIELDSET);

                if (property.getItems().size() == 1) {
                    Element layout = document.createElement("layout");
                    layout.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
                    layout.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_FIXEDCOLUMNS);
                    layout.setAttribute("method", "absolute");
                    field.appendChild(layout);

                    Node items = field.appendChild(createUnStructuredNode(document, "items"));
                    Element column = document.createElement("column");
                    column.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
                    column.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_CONTAINER);
                    items.appendChild(column);

                    items = column.appendChild(createUnStructuredNode(document, "items"));

                    Element actualField = document.createElement("field");
                    actualField.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
                    actualField.setAttribute(Constants.PROPERTY_NAME, "./" + property.getField());
                    actualField.setAttribute(Constants.PROPERTY_CQ_MSM_LOCKABLE, "./" + property.getField());

                    Property prop = property.getItems().get(0);
                    Optional.ofNullable(getSlingResourceType(prop.getType())).ifPresent(resType -> {
                        actualField.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, resType);
                    });
                    addBasicProperties(actualField, prop);
                    processAttributes(actualField, prop);
                    items.appendChild(actualField);
                } else {
                    propertyNode.setAttribute(Constants.PROPERTY_COMPOSITE, "{Boolean}true");
                    Element items = createUnStructuredNode(document, "items");
                    field.appendChild(items);
                    processItems(document, items, property);
                }

                propertyNode.appendChild(field);
            }
        }

        // only append the primary property field after successfully constructing its members
        currentNode.appendChild(propertyNode);
    }

    /**
     * Processes the attributes for a propertyNode.
     *
     * @param propertyNode The node to add property attributes
     * @param property The {@link Property} object contains attributes
     */
    private static void processAttributes(Element propertyNode, Property property) {
        if (property.getAttributes() != null && property.getAttributes().size() > 0) {
            property.getAttributes().entrySet().stream()
                    .forEach(entry -> propertyNode.setAttribute(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Process the dialog node item by setting property attributes on it.
     *
     * @param document The {@link Document} object
     * @param itemsNode The parent {@link Element} object
     * @param property The {@link Property} object contains attributes
     */
    private static void processItems(Document document, Element itemsNode, Property property) {
        for (Property item : property.getItems()) {
            Element optionNode = document.createElement(item.getField());
            optionNode.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);

            addBasicProperties(optionNode, item);

            String resourceType = getSlingResourceType(item.getType());
            if (StringUtils.isNotEmpty(resourceType)) {
                optionNode.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, resourceType);
            }

            if (StringUtils.equalsIgnoreCase("multifield", property.getType())) {
                getPrimaryFieldName(item).ifPresent(name -> {
                    optionNode.setAttribute(Constants.PROPERTY_NAME, name);
                });

                if ("image".equalsIgnoreCase(item.getType())) {
                    addImageHiddenProperyValues(document, itemsNode, item);

                    // add these default image attributes BEFORE generic attribute handling, to allow for
                    // individual overrides
                    addImagePropertyValues(optionNode, item);
                }
            }

            processAttributes(optionNode, item);
            itemsNode.appendChild(optionNode);
        }
    }

    /**
     * Adds the field label and field description attributes to the node.
     *
     * @param propertyNode The node to add property attributes
     * @param property The {@link Property} object contains attributes
     */
    private static void addBasicProperties(Element propertyNode, Property property) {
        if (StringUtils.isNotEmpty(property.getLabel())) {
            if (property.getType().equalsIgnoreCase(Constants.TYPE_HEADING)) {
                propertyNode.setAttribute(Constants.PROPERTY_TEXT, property.getLabel());
            } else {
                propertyNode.setAttribute(Constants.PROPERTY_FIELDLABEL, property.getLabel());
            }
        }
        if (StringUtils.isNotEmpty(property.getDescription())) {
            propertyNode.setAttribute(Constants.PROPERTY_FIELDDESC, property.getDescription());
        }
    }

    /**
     * Adds the properties specific to the image node. These could all have been
     * included as attributes in the configuration json file, but they never/rarely
     * change, so hardcoding them here seems safe to do.
     *
     * @param imageNode The {@link Node} object
     * @param property The {@link Property} object contains attributes
     */
    private static void addImagePropertyValues(Element imageNode, Property property) {
        imageNode.setAttribute("allowUpload", "{Boolean}false");
        imageNode.setAttribute("autoStart", "{Boolean}false");
        imageNode.setAttribute("class", "cq-droptarget");
        imageNode.setAttribute("fileNameParameter", "./" + property.getField() + "/fileName");
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
     * @param document the host document
     * @param parentElement An {@link Element} object that an image's
     *            hidden node should be added as a child to
     * @param property The {@link Property} object contains attributes
     */
    private static void addImageHiddenProperyValues(Document document, Element parentElement, Property property) {
        Element hiddenImageNode = document.createElement(property.getField() + "ResType");
        hiddenImageNode.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        hiddenImageNode.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_HIDDEN);
        hiddenImageNode.setAttribute("name", "./" + property.getField() + "/" + Constants.PROPERTY_SLING_RESOURCETYPE);
        hiddenImageNode.setAttribute("value", Constants.RESOURCE_TYPE_IMAGE_HIDDEN_TYPE);
        // add this hidden field to the parent
        parentElement.appendChild(hiddenImageNode);
    }

    /**
     * Builds default node structure of dialog xml in the document passed in based
     * on dialogType.
     *
     * @param document The {@link Document} object
     * @param root The root node to append children nodes to
     * @return Node
     */
    private static Element updateDefaultNodeStructure(Document document, Element root) {
        Element containerElement = createNode(document, "content", Constants.RESOURCE_TYPE_CONTAINER);

        Element layoutElement = createNode(document, "layout", Constants.RESOURCE_TYPE_FIXEDCOLUMNS);
        layoutElement.setAttribute("margin", "{Boolean}false");

        Element columnElement = createNode(document, "column", Constants.RESOURCE_TYPE_CONTAINER);

        root.appendChild(containerElement);

        containerElement.appendChild(layoutElement);
        Element topItemsElement = createUnStructuredNode(document, "items");
        Element bottomItemsElement = createUnStructuredNode(document, "items");
        containerElement.appendChild(topItemsElement).appendChild(columnElement).appendChild(bottomItemsElement);
        return bottomItemsElement;
    }

    /**
     * Creates the default tab node structure.
     *
     * @param document The {@link Document} object
     * @param root The {@link Element} object
     * @return the node
     */
    private static Element createTabsParentNodeStructure(Document document, Element root) {
        Element containerElement = createNode(document, "content", Constants.RESOURCE_TYPE_CONTAINER);
        root.appendChild(containerElement);
        Element topItemsElement = createUnStructuredNode(document, "items");
        Element tabsElement = createNode(document, "tabs", Constants.RESOURCE_TYPE_TABS);
        Element bottomItemsElement = createUnStructuredNode(document, "items");
        containerElement.appendChild(topItemsElement).appendChild(tabsElement).appendChild(bottomItemsElement);

        return bottomItemsElement;
    }

    /**
     * Creates the tab structure.
     *
     * @param document The {@link Document} object
     * @param tab the tab
     * @param parentElement The parent {@link Element} object
     * @return the node {@link Node} object
     */
    private static Element createTabStructure(Document document, Tab tab, Element parentElement) {
        Element tabElement = createNode(document, tab.getId(), Constants.RESOURCE_TYPE_CONTAINER);
        String label = tab.getLabel();
        if (StringUtils.isBlank(label)) {
            label = CaseUtils.toCamelCase(tab.getId(), true);
        }
        tabElement.setAttribute(Constants.PROPERTY_JCR_TITLE, label);
        Element columnElement = createNode(document, "column", Constants.RESOURCE_TYPE_CONTAINER);

        Element layoutElement = document.createElement("layout");
        layoutElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        layoutElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_CORAL_FIXEDCOLUMNS);

        tabElement.appendChild(layoutElement);

        Element topItemsElement = createUnStructuredNode(document, "items");
        Element bottomItemsElement = createUnStructuredNode(document, "items");
        parentElement.appendChild(tabElement).appendChild(topItemsElement)
                .appendChild(columnElement).appendChild(bottomItemsElement);
        return bottomItemsElement;
    }

    /**
     * Creates a node with the jcr:primaryType set to nt:unstructured.
     *
     * @param document The {@link Document} object
     * @param nodeName The name of the node being created
     * @return Node
     */
    protected static Element createUnStructuredNode(Document document, String nodeName) {
        Element element = document.createElement(nodeName);
        element.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        return element;
    }

    /**
     * Creates the node.
     *
     * @param document The {@link Document} object
     * @param fieldName the field name
     * @param resourceType the resource type
     * @return An {@link Element} object
     */
    private static Element createNode(Document document, String fieldName, String resourceType) {
        Element containerElement = document.createElement(fieldName);
        containerElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        containerElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, resourceType);
        return containerElement;
    }

    /**
     * Return the submittable, lockable, primary field name for the given property. Fields that
     * aren't defined or directly submittable will return empty. Image type wraps a fileupload field,
     * which is expected to post to a "file" subresource relative to Image base resource.
     *
     * @param property the current property
     * @return the appropriate field relative path name or empty
     */
    private static Optional<String> getPrimaryFieldName(Property property) {
        final String type = property.getType();
        if (StringUtils.isBlank(type)
                || Constants.TYPE_HEADING.equalsIgnoreCase(type)
                || "multifield".equalsIgnoreCase(type)) {
            return Optional.empty();
        }
        if ("image".equalsIgnoreCase(type)) {
            return Optional.of("./" + property.getField() + "/file");
        } else {
            return Optional.of("./" + property.getField());
        }
    }

    /**
     * Determine the proper sling:resourceType.
     *
     * @param type The sling:resourceType
     * @return String
     */
    private static String getSlingResourceType(String type) {
        if (StringUtils.isNotBlank(type)) {
            if (StringUtils.equalsIgnoreCase("textfield", type)) {
                return Constants.RESOURCE_TYPE_TEXTFIELD;
            } else if (StringUtils.equalsIgnoreCase("numberfield", type)) {
                return Constants.RESOURCE_TYPE_NUMBER;
            } else if (StringUtils.equalsIgnoreCase("checkbox", type)) {
                return Constants.RESOURCE_TYPE_CHECKBOX;
            } else if (StringUtils.equalsIgnoreCase("pagefield", type)) {
                return Constants.RESOURCE_TYPE_PAGEFIELD;
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
            } else if (StringUtils.equalsIgnoreCase("image", type)) {
                return Constants.RESOURCE_TYPE_IMAGE;
            } else if (StringUtils.equalsIgnoreCase("multifield", type)) {
                return Constants.RESOURCE_TYPE_MULTIFIELD;
            } else if (StringUtils.equalsIgnoreCase("tagfield", type)) {
                return Constants.RESOURCE_TYPE_TAGFIELD;
            } else if (StringUtils.equalsIgnoreCase(Constants.TYPE_HEADING, type)) {
                return Constants.RESOURCE_TYPE_HEADING;
            }
        }
        return null;
    }
}
