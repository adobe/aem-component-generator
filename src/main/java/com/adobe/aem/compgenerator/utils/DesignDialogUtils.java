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
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DesignDialogUtils extends DialogUtils {

    /**
     * Creates the design dialog xml by adding the style system tab.
     *
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     */
    public static void createDesignDialogXml(final GenerationConfig generationConfig, String dialogType) {
        try {
            String designDialogPath = generationConfig.getCompDir() + "/" + Constants.DIALOG_TYPE_DESIGN_DIALOG;
            CommonUtils.createFolder(designDialogPath);

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = createDialogRoot(doc, generationConfig, dialogType);
            buildDesignDialogStructure(doc, rootElement);

            doc.appendChild(rootElement);
            XMLUtils.transformDomToFile(doc, designDialogPath + "/" + Constants.FILENAME_CONTENT_XML);
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating Design Dialog xml.");
        }
    }

    /**
     * Fully builds the design dialog's structure on the {@link Element} 'root'.
     *
     * @param document The {@link Document} object
     * @param root The root node to append children nodes to
     */
    private static void buildDesignDialogStructure(Document document, Element root) {
        Element containerElement = document.createElement("content");
        containerElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        containerElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_CONTAINER);

        Element tabsElement = document.createElement("tabs");
        tabsElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        tabsElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_TABS);
        tabsElement.setAttribute("maximized", "{Boolean}true");

        Element styleTabElement = document.createElement("styletab");
        styleTabElement.setAttribute(Constants.JCR_PRIMARY_TYPE, Constants.NT_UNSTRUCTURED);
        styleTabElement.setAttribute(Constants.PROPERTY_SLING_RESOURCETYPE, Constants.RESOURCE_TYPE_INCLUDE);
        styleTabElement.setAttribute("path", Constants.STYLE_SYSTEM_TAB_PATH);

        root.appendChild(containerElement)
                .appendChild(createUnStructuredNode(document, "items"))
                .appendChild(tabsElement)
                .appendChild(createUnStructuredNode(document, "items"))
                .appendChild(styleTabElement);
    }
}
