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

import com.adobe.aem.compgenerator.Constants;
import com.adobe.aem.compgenerator.exceptions.GeneratorException;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.Map;

/**
 * <p>
 * ComponentUtils class helps in building elements of component
 * like folders, xml file, html with content based data-config file.
 */

public class ComponentUtils {

    private static final Logger LOG = LogManager.getLogger(ComponentUtils.class);

    private GenerationConfig generationConfig;
    private Map<String, String> templateValueMap;

    public ComponentUtils(GenerationConfig config) {
        this.generationConfig = config;
        this.templateValueMap = CommonUtils.getTemplateValueMap(generationConfig);
    }

    /**
     * builds your base folder structure of a component includes component folder
     * itself, _cq_dialog with field properties, dialogglobal with properties-global,
     * HTML, clientlibs folder.
     */
    public void buildComponent() throws Exception {
        if (generationConfig == null) {
            throw new GeneratorException("Config file cannot be empty / null !!");
        }

        //creates base component folder.
        createFolderWithContentXML(generationConfig.getCompDir(), Constants.TYPE_COMPONENT);

        //create _cq_dialog xml with user input properties in json.
        DialogUtils.createDialogXml(generationConfig, Constants.DIALOG_TYPE_DIALOG);

        //create dialogglobal xml file with user input global properties in json.
        if (generationConfig.getOptions().getGlobalProperties() != null &&
                !generationConfig.getOptions().getGlobalProperties().isEmpty()) {
            DialogUtils.createDialogXml(generationConfig, Constants.DIALOG_TYPE_GLOBAL);
        }

        //create dialogshared xml file with user input global properties in json.
        if (generationConfig.getOptions().getSharedProperties() != null &&
                !generationConfig.getOptions().getSharedProperties().isEmpty()) {
            DialogUtils.createDialogXml(generationConfig, Constants.DIALOG_TYPE_SHARED);
        }

        //builds clientLib and placeholder files for js and css.
        createClientLibs();

        //builds sightly html file using htl template from resource.
        createHtl();

        LOG.info("--------------* Component '" + generationConfig.getName() + "' successfully generated *--------------");

    }

    /**
     * builds default clientlib structure with js and css file under folder.
     */
    private void createClientLibs() {
        String clientLibDirPath = generationConfig.getCompDir() + "/clientlibs";
        try {
            if (generationConfig.getOptions().isHasJs() || generationConfig.getOptions().isHasCss()) {
                createFolderWithContentXML(clientLibDirPath, Constants.TYPE_SLING_FOLDER);

                String clientLibSiteDirPath = clientLibDirPath + "/site";
                createFolderWithContentXML(clientLibSiteDirPath, Constants.TYPE_CLIENTLIB_FOLDER);

                if (generationConfig.getOptions().isHasCss()) {
                    String clientLibCssFolder = clientLibSiteDirPath + "/css";
                    CommonUtils.createFolder(clientLibCssFolder);

                    String clientLibCssFileName = generationConfig.getName() + ".less";
                    String clientLibCssFilePath = clientLibCssFolder + "/" + clientLibCssFileName;
                    CommonUtils.createFileWithCopyRight(clientLibCssFilePath, templateValueMap);

                    if (generationConfig.getOptions().isHasCssText()) {
                        String clientLibCssTextFile = clientLibSiteDirPath + "/css.txt";
                        CommonUtils.createClientlibTextFile(clientLibCssTextFile, clientLibCssFileName);
                    }
                }

                if (generationConfig.getOptions().isHasJs()) {
                    String clientLibJsFolder = clientLibSiteDirPath + "/js";
                    CommonUtils.createFolder(clientLibJsFolder);

                    String clientLibJsFileName = generationConfig.getName() + ".js";
                    String clientLibJsFilePath = clientLibJsFolder + "/" + clientLibJsFileName;
                    CommonUtils.createFileWithCopyRight(clientLibJsFilePath, templateValueMap);

                    if (generationConfig.getOptions().isHasJsText()) {
                        String clientLibJsTextFile = clientLibSiteDirPath + "/js.txt";
                        CommonUtils.createClientlibTextFile(clientLibJsTextFile, clientLibJsFileName);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new GeneratorException("Exception while creating clientLibs : " + clientLibDirPath);
        }
    }

    /**
     * Creates a folder on given path and adds content.xml file based on the folderType.
     *
     * @param path Full path including the new file name
     * @param folderType The 'jcr:primaryType' of the folder
     * @throws Exception exception
     */
    private void createFolderWithContentXML(String path, String folderType) throws Exception {
        Path folderPath = CommonUtils.createFolder(path);
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = XMLUtils.createRootElement(doc);

            //set attributes based on folderType.
            if (folderType.equalsIgnoreCase(Constants.TYPE_COMPONENT)) {
                rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, folderType);
                rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE, generationConfig.getTitle());
                rootElement.setAttribute("componentGroup", generationConfig.getGroup());
            } else if (folderType.equalsIgnoreCase(Constants.TYPE_SLING_FOLDER)) {
                rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, folderType);
            } else if (folderType.equalsIgnoreCase(Constants.TYPE_CLIENTLIB_FOLDER)) {
                rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, folderType);
                rootElement.setAttribute("categories", "[components." + generationConfig.getName() + "]");
                rootElement.setAttribute("allowProxy", "{Boolean}true");
            }

            doc.appendChild(rootElement);
            XMLUtils.transformDomToFile(doc, folderPath + "/" + Constants.FILENAME_CONTENT_XML);
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating Folder/xml : " + path);
        }
    }

    /**
     * Create default HTML file based the provided template.
     */
    private void createHtl() {
        try {
            CommonUtils.createFileWithCopyRight(generationConfig.getCompDir()
                    + "/" + generationConfig.getName() + ".html", templateValueMap);
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating HTML : " + generationConfig.getCompDir());
        }
    }
}
