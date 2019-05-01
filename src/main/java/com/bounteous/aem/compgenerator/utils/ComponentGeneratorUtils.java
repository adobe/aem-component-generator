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
import org.apache.commons.lang.text.StrSubstitutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * ComponentGeneratorUtils class helps in building elements of component
 * like folders, xml file, html with content based data-config file.
 */

public class ComponentGeneratorUtils {

    private GenerationConfig generationConfig;
    private Map<String, String> templateValueMap;

    public ComponentGeneratorUtils(GenerationConfig config) {
        this.generationConfig = config;
        this.templateValueMap = getTemplateValueMap();
    }

    /**
     * builds your base folder structure of a component includes component folder
     * itself, _cq_dialog with field properties, dialogGlobal with properties-global,
     * HTML, clientlibs folder.
     */
    public void _buildComponent() throws Exception {
        if (generationConfig == null) {
            throw new GeneratorException("Config file cannot be empty / null !!");
        }

        //creates base component folder.
        createFolderWithContentXML(generationConfig.getCompDir(), Constants.TYPE_COMPONENT);

        //create _cq_dialog xml with user input properties in json.
        createDialogXml(Constants.DIALOG_TYPE_DIALOG);

        //create dialogGlobal xml file with user input global properties in json.
        if (generationConfig.getOptions().getGlobalProperties() != null &&
                generationConfig.getOptions().getGlobalProperties().size() > 0) {
            createDialogXml(Constants.DIALOG_TYPE_GLOBAL);
        }

        //create dialogGlobal xml file with user input global properties in json.
        if (generationConfig.getOptions().getSharedProperties() != null &&
                generationConfig.getOptions().getSharedProperties().size() > 0) {
            createDialogXml(Constants.DIALOG_TYPE_SHARED);
        }

        //builds clientLib and placeholder files for js and css.
        createClientLibs();

        //builds sightly html file using htl template from resource.
        createHtl();

        System.out.println("--------------* Component '" + generationConfig.getName() + "' successfully generated *--------------");

    }

    /**
     * builds default clientlib structure with js and css file under folder.
     */
    private void createClientLibs() {
        String clientLibDirPath = generationConfig.getCompDir() + "/clientlibs";
        try {
            if (generationConfig.getOptions().isHasJs() || generationConfig.getOptions().isHasCss()) {
                createFolderWithContentXML(clientLibDirPath, Constants.TYPE_SLING_FOLDER);
                if (generationConfig.getOptions().isHasCss()) {
                    createFolder(clientLibDirPath + "/site/css");
                    createFileWithCopyRight(clientLibDirPath + "/site/css/" + generationConfig.getName() + ".less");
                }
                if (generationConfig.getOptions().isHasJs()) {
                    createFolder(clientLibDirPath + "/site/js");
                    createFileWithCopyRight(clientLibDirPath + "/site/js/" + generationConfig.getName() + ".js");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneratorException("Exception while creating clientLibs : " + clientLibDirPath);
        }
    }

    /**
     * creates dialog xml by adding the properties in data-config json file.
     *
     * @param dialogType dialogType to dialog xml structure.
     */
    private void createDialogXml(final String dialogType) {
        String dialogPath = generationConfig.getCompDir() + Constants.SYMBOL_SLASH + dialogType;
        try {
            ComponentGeneratorUtils.createFolder(dialogPath);

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

                properties.stream()
                        .filter(Objects::nonNull)
                        .map(property -> createPropertyNode(doc, property))
                        .filter(Objects::nonNull)
                        .forEach(a -> currentNode.appendChild(a));
            }
            doc.appendChild(rootElement);
            transformDomToFile(doc, dialogPath + Constants.SYMBOL_SLASH + Constants.FILENAME_CONTENT_XML);
            System.out.println("Created : " + dialogPath + Constants.SYMBOL_SLASH + Constants.FILENAME_CONTENT_XML);
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating Dialog xml : " + dialogPath);
        }
    }

    /**
     * adds a dialog property xml node with all input attr under the document.
     *
     * @param document
     * @param property project object contains attributes.
     * @return
     */
    private Element createPropertyNode(Document document, Property property) {
        try {
            Element propertyNode = document.createElement(property.getField());

            propertyNode.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
            propertyNode.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, getSlingResourceType(property.getType()));
            propertyNode.setAttribute(Constants.PROPERTY_CQ_MSM_LOCKABLE, "./" + property.getField());
            propertyNode.setAttribute(Constants.PROPERTY_FIELDLABEL, property.getLabel());
            propertyNode.setAttribute(Constants.PROPERTY_NAME, "./" + property.getField());

            if (property.getAttributes() != null && property.getAttributes().size() > 0) {
                property.getAttributes()
                        .entrySet()
                        .stream()
                        .forEach(entry -> propertyNode.setAttribute(entry.getKey(), entry.getValue()));
            }
            return propertyNode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Element createDialogRoot(Document document, GenerationConfig generationConfig, String dialogType) {
        Element rootElement = createRootElement(document);
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
     * builds default node structure of dialog xml in the document passed in based on dialogType.
     *
     * @param document
     * @param root
     * @param dialogType
     * @return
     */
    private Node updateDefaultNodeStructure(Document document, Element root, String dialogType) {
        Element containerElement = document.createElement("content");
        containerElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        containerElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_CONTAINER);

        Element layoutElement = document.createElement("layout");
        layoutElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        layoutElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_TABS);
        layoutElement.setAttribute("type", "nav");

        Element contentElement = document.createElement("content");
        contentElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        contentElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_SECTION);

        Element layoutElement1 = document.createElement("layout");
        layoutElement1.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        layoutElement1.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_FIXEDCOLUMNS);
        layoutElement1.setAttribute("margin", "{Boolean}false");

        Element columnElement = document.createElement("column");
        columnElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        columnElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_CONTAINER);

        Node containerNode = root.appendChild(containerElement);

        if (dialogType.equalsIgnoreCase(Constants.DIALOG_TYPE_GLOBAL)) {
            containerNode.appendChild(layoutElement1);
            return containerNode
                    .appendChild(createUnStructuredNode(document, "items"))
                    .appendChild(columnElement)
                    .appendChild(createUnStructuredNode(document, "items"));
        }

        containerNode.appendChild(layoutElement);
        Node sectionNode = containerNode
                .appendChild(createUnStructuredNode(document, "items"))
                .appendChild(contentElement);
        sectionNode.appendChild(layoutElement1);

        return sectionNode
                .appendChild(createUnStructuredNode(document, "items"))
                .appendChild(columnElement)
                .appendChild(createUnStructuredNode(document, "items"));
    }

    private static Path createFolder(String folderPath) throws Exception {
        Path path = Paths.get(folderPath);
        if (Files.notExists(path)) {
            return Files.createDirectories(path);
        }
        return path;
    }

    private Node createUnStructuredNode(Document document, String nodeName) {
        Element element = document.createElement(nodeName);
        element.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        return element;
    }

    /**
     * creates a folder on given path and adds content.xml file based on the folderType.
     *
     * @param path
     * @param folderType
     * @throws Exception
     */
    private void createFolderWithContentXML(String path, String folderType)
            throws Exception {
        Path folderPath = createFolder(path);
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = createRootElement(doc);

            //set attributes based on folderType.
            if (folderType.equalsIgnoreCase(Constants.TYPE_COMPONENT)) {
                rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, folderType);
                rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE, generationConfig.getTitle());
                rootElement.setAttribute("componentGroup", generationConfig.getGroup());
            } else if (folderType.equalsIgnoreCase(Constants.TYPE_SLING_FOLDER)) {
                rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, folderType);
            }
            doc.appendChild(rootElement);
            transformDomToFile(doc, folderPath + Constants.SYMBOL_SLASH + Constants.FILENAME_CONTENT_XML);
            System.out.println("Created : " + folderPath + Constants.SYMBOL_SLASH + Constants.FILENAME_CONTENT_XML);
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating Folder/xml : " + path);
        }
    }

    /**
     * creates root node with of dialog xml with required name spaces as attr.
     *
     * @param document
     * @return
     */
    private Element createRootElement(Document document) {
        if (document == null) {
            return null;
        }

        document.appendChild(document.createComment(CommonUtils.getResourceContentAsString(Constants.TEMPLATE_COPYRIGHT_XML)));
        Element rootElement = document.createElement(Constants.JCR_ROOT_NODE);
        rootElement.setAttribute("xmlns:sling", "http://sling.apache.org/jcr/sling/1.0");
        rootElement.setAttribute("xmlns:cq", "http://www.day.com/jcr/cq/1.0");
        rootElement.setAttribute("xmlns:jcr", "http://www.jcp.org/jcr/1.0");
        rootElement.setAttribute("xmlns:nt", "http://www.jcp.org/jcr/nt/1.0");

        return rootElement;
    }

    /**
     * create default HTML file based the provided template.
     */
    private void createHtl() {
        try {
            createFileWithCopyRight(generationConfig.getCompDir() +
                    Constants.SYMBOL_SLASH + generationConfig.getName() + ".html");

            System.out.println("Created : " + generationConfig.getCompDir() +
                    Constants.SYMBOL_SLASH + generationConfig.getName() + ".html");
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating HTML : " + generationConfig.getCompDir());
        }
    }

    /**
     * method will transform Document structure by prettify xml elements to file.
     *
     * @param document
     * @param filePath
     */
    private static void transformDomToFile(Document document, String filePath) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer tr = tf.newTransformer();

            //config for beautify/prettify xml content.
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filePath));

            //transform your DOM source to the given file location.
            tr.transform(source, result);

        } catch (Exception e) {
            throw new GeneratorException("Exception while DOM conversion to file : " + filePath);
        }
    }

    /**
     * creates a map of values required for any template. Let's say htl template and others if any.
     *
     * @return map
     */
    private Map<String, String> getTemplateValueMap() {
        if (generationConfig != null) {
            Map<String, String> map = new HashMap<>();
            map.put("name", generationConfig.getName());
            map.put("title", generationConfig.getTitle());
            map.put("sightly", StringUtils.uncapitalize(generationConfig.getJavaFormatedName()));
            map.put("slingModel", Constants.PACKAGE_MODELS + "." + generationConfig.getJavaFormatedName());
            return map;
        }
        return null;
    }

    private void createFileWithCopyRight(String path) throws IOException {
        File file = new File(path);
        if (file != null) {
            String template = Constants.TEMPLATE_COPYRIGHT_JAVA;
            if (path.endsWith("js") || path.endsWith("java")) {
                template = Constants.TEMPLATE_COPYRIGHT_JAVA;
            } else if (path.endsWith("less")) {
                template = Constants.TEMPLATE_COPYRIGHT_CSS;
            } else if (path.endsWith("xml")) {
                template = Constants.TEMPLATE_COPYRIGHT_XML;
            } else if (path.endsWith("html")) {
                template = Constants.TEMPLATE_HTL;
            }

            StrSubstitutor strSubstitutor = new StrSubstitutor(this.templateValueMap);
            String templateString = CommonUtils.getResourceContentAsString(template);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(strSubstitutor.replace(templateString));
            writer.close();
        }
    }

    private String getSlingResourceType(String type){
        if(StringUtils.isNotBlank(type)){
            if (StringUtils.equalsIgnoreCase("text", type)) {
                return Constants.RESOURCE_TYPE_TEXTFIELD;
            } else if (StringUtils.equalsIgnoreCase("number", type)) {
                return Constants.RESOURCE_TYPE_NUMBER;
            } else if (StringUtils.equalsIgnoreCase("checkbox", type)) {
                return Constants.RESOURCE_TYPE_CHECKBOX;
            }
        }
        return null;
    }

}

