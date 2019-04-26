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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * ComponentGeneratorUtils class helps in building elements of component
 * like folders, xml file, html with content based data-config file.
 */

public class ComponentGeneratorUtils {

    /**
     * Method to map JSON content from given file into given GenerationConfig type.
     *
     * @param jsonDataFile data-config file.
     * @return GenerationConfig java class with the mapped content in json file.
     */
    public static GenerationConfig getComponentData(File jsonDataFile) {
        if (jsonDataFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return mapper.readValue(jsonDataFile, GenerationConfig.class);
            } catch (IOException e) {
                throw new GeneratorException("Exception while reading config file.");
            }
        }
        return null;
    }

    /**
     * builds your base folder structure of a component includes component folder
     * itself, _cq_dialog with field properties, dialogGlobal with properties-global,
     * HTML, clientlibs folder.
     *
     * @param generationConfig data-config java object.
     */
    public static void _buildComponent(GenerationConfig generationConfig) throws Exception {
        if (generationConfig == null) {
            throw new GeneratorException("Config file cannot be empty / null !!");
        }
        if (StringUtils.isBlank(generationConfig.getName()) || StringUtils.isBlank(generationConfig.getType())) {
            throw new GeneratorException("Mandatory fields missing in the data-config.json !");
        }

        String compDir = Constants.PROJECT_COMPONENT + "/" + generationConfig.getType() + "/" + generationConfig.getName();
        generationConfig.setCompDir(compDir);

        //creates base component folder.
        createFolderWithContentXML(generationConfig, compDir, Constants.TYPE_COMPONNET);

        //create _cq_dialog xml with user input properties in json.
        createDialogXml(generationConfig, "dialog");

        //create dialogGlobal xml file with user input global properties in json.
        if (generationConfig.getOptions().getGobalProperties() != null &&
                generationConfig.getOptions().getGobalProperties().size() > 0) {
            createDialogXml(generationConfig, Constants.FILENAME_DIALOG_GLOBAL);
        }

        //builds clientLib and placeholder files for js and css.
        createClientLibs(generationConfig);

        //builds sightly html file using htl template from resource.
        createHtl(generationConfig);

        System.out.println("--------------* Component '" + generationConfig.getName() + "' successfully generated *--------------");

    }

    /**
     * builds default clientlib structure with js and css file under folder.
     * @param generationConfig
     */
    public static void createClientLibs(GenerationConfig generationConfig) {
        String clientLibPathDirs = generationConfig.getCompDir() + "/clientlibs";
        try {
            if (generationConfig.getOptions().isHasJs() || generationConfig.getOptions().isHasCss()) {
                createFolderWithContentXML(generationConfig, clientLibPathDirs, Constants.TYPE_SLING_FOLDER);
                if (generationConfig.getOptions().isHasCss()) {
                    createFolder(clientLibPathDirs + "/site/css");
                    new File(clientLibPathDirs + "/site/css/" + generationConfig.getName() + ".less")
                            .createNewFile();
                }
                if (generationConfig.getOptions().isHasJs()) {
                    createFolder(clientLibPathDirs + "/site/js");
                    new File(clientLibPathDirs + "/site/js/" + generationConfig.getName() + ".js")
                            .createNewFile();

                }
            }
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating clientLibs : " + clientLibPathDirs);
        }
    }

    /**
     * creates dilaog xml by adding the properties in data-conifg json file.
     * @param generationConfig
     * @param dialogType
     */
    public static void createDialogXml(GenerationConfig generationConfig, String dialogType) {
        String dialogPath;

        if (dialogType.equalsIgnoreCase(Constants.FILENAME_DIALOG_GLOBAL)) {
            dialogPath = generationConfig.getCompDir() + Constants.SYMBOL_SLASH + Constants.FILENAME_DIALOG_GLOBAL;
        } else {
            dialogPath = generationConfig.getCompDir() + Constants.SYMBOL_SLASH + Constants.FILENAME_DIALOG;
        }

        try {
            ComponentGeneratorUtils.createFolder(dialogPath);

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element rootElement = createDialogRoot(doc, generationConfig, dialogType);

            List<Property> properties = generationConfig.getOptions().getProperties();
            if (dialogType.equalsIgnoreCase(Constants.FILENAME_DIALOG_GLOBAL)) {
                properties = generationConfig.getOptions().getGobalProperties();
            }

            if (properties != null && properties.size() > 0) {
                Node currentNode = updateDefaultNodeStructure(doc, rootElement, dialogType);

                properties.stream()
                        .filter(Objects::nonNull)
                        .map(property -> ComponentGeneratorUtils.createPropertyNode(doc, property))
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
     * @param document
     * @param property
     * @return
     */
    private static Element createPropertyNode(Document document, Property property) {
        try {
            Element propertyNode = document.createElement(property.getField());

            propertyNode.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);

            if (property.getType().equalsIgnoreCase("text")) {
                propertyNode.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_TEXTFIELD);
            } else if (property.getType().equalsIgnoreCase("number")) {
                propertyNode.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_NUMBER);
            }

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

    private static Element createDialogRoot(Document document, GenerationConfig generationConfig, String dialogType) {
        Element rootElement = createRootElement(document);
        rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        rootElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_DIALOG);
        if (dialogType.equalsIgnoreCase(Constants.FILENAME_DIALOG_GLOBAL)) {
            rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE,
                    generationConfig.getTitle() + " (Global Properties)");
        } else {
            rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE, generationConfig.getTitle());
        }

        return rootElement;
    }

    /**
     * builds default node structure of dialog xml in the document passed in based on dialogType.
     * @param document
     * @param root
     * @param dialogType
     * @return
     */
    private static Node updateDefaultNodeStructure(Document document, Element root, String dialogType) {
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

        if (dialogType.equalsIgnoreCase(Constants.FILENAME_DIALOG_GLOBAL)) {
            return root.appendChild(containerElement)
                    .appendChild(layoutElement1)
                    .appendChild(createUnStructuredNode(document, "items"))
                    .appendChild(columnElement)
                    .appendChild(createUnStructuredNode(document, "items"));
        }
        return root.appendChild(containerElement)
                .appendChild(layoutElement)
                .appendChild(createUnStructuredNode(document, "items"))
                .appendChild(contentElement)
                .appendChild(layoutElement1)
                .appendChild(createUnStructuredNode(document, "items"))
                .appendChild(columnElement)
                .appendChild(createUnStructuredNode(document, "items"));
    }

    public static Path createFolder(String folderPath) throws Exception {
        Path path = Paths.get(folderPath);
        if (Files.notExists(path)) {
            return Files.createDirectories(path);
        }
        return path;
    }

    private static Node createUnStructuredNode(Document document, String nodeName) {
        Element element = document.createElement(nodeName);
        element.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        return element;
    }

    /**
     * creates a folder on given path and adds content.xml file based on the folderType.
     * @param generationConfig
     * @param path
     * @param folderType
     * @throws Exception
     */
    public static void createFolderWithContentXML(GenerationConfig generationConfig, String path, String folderType)
            throws Exception {
        Path folderPath = createFolder(path);
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = createRootElement(doc);

            //set attributes based on folderType.
            if (folderType.equalsIgnoreCase(Constants.TYPE_COMPONNET)) {
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
     * @param document
     * @return
     */
    public static Element createRootElement(Document document) {
        if (document == null) {
            return null;
        }

        document.appendChild(document.createComment(getResourceContentAsString(Constants.TEMPLATE_COPYRIGHT_XML)));
        Element rootElement = document.createElement(Constants.JCR_ROOT_NODE);
        rootElement.setAttribute("xmlns:sling", "http://sling.apache.org/jcr/sling/1.0");
        rootElement.setAttribute("xmlns:cq", "http://www.day.com/jcr/cq/1.0");
        rootElement.setAttribute("xmlns:jcr", "http://www.jcp.org/jcr/1.0");
        rootElement.setAttribute("xmlns:nt", "http://www.jcp.org/jcr/nt/1.0");

        return rootElement;
    }

    /**
     * create default HTML file based the provided template.
     * @param generationConfig
     */
    public static void createHtl(GenerationConfig generationConfig) {
        try {
            StrSubstitutor strSubstitutor = new StrSubstitutor(getTemplateValueMap(generationConfig));
            String slyHtml = getResourceContentAsString(Constants.TEMPLATE_COPYRIGHT_XML);

            BufferedWriter writer = new BufferedWriter(new FileWriter(generationConfig.getCompDir() +
                    Constants.SYMBOL_SLASH + generationConfig.getName() + ".html"));
            writer.write(strSubstitutor.replace(slyHtml));
            writer.close();

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
     * @param generationConfig
     * @return map
     */
    private static Map<String, String> getTemplateValueMap(GenerationConfig generationConfig) {
        if (generationConfig != null) {
            Map<String, String> map = new HashMap<>();
            map.put("name", generationConfig.getName());
            map.put("sightly", generationConfig.getJavaFormatedName());
            map.put("slingModel", Constants.PACKAGE_MODELS + "." + generationConfig.getJavaFormatedName());
            return map;
        }
        return null;
    }

    /**
     * method to read the content of any resource file in the project as string.
     * @param filePath
     * @return
     */
    public static String getResourceContentAsString(String filePath) {
        try (InputStream inputStream = ComponentGeneratorUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * method checks if the file exists and not empty.
     *
     * @param file
     * @return boolean
     */
    public static boolean isFileBlank(File file) {
        return file.exists() && file.length() == 0 ? true : false;
    }

}

