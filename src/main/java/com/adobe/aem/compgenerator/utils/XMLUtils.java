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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XMLUtils {

    /**
     * Creates root node with of dialog xml with required name spaces as attr.
     *
     * @param document The {@link Document} object
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     * @return Element
     */
    public static Element createRootElement(Document document, GenerationConfig generationConfig) {
        if (document == null) {
            return null;
        }

        String templateString = CommonUtils.getTemplateFileAsString(Constants.TEMPLATE_COPYRIGHT_XML, generationConfig);
        document.appendChild(document.createComment(templateString));
        Element rootElement = document.createElement(Constants.JCR_ROOT_NODE);
        rootElement.setAttribute("xmlns:sling", "http://sling.apache.org/jcr/sling/1.0");
        rootElement.setAttribute("xmlns:granite", "http://www.adobe.com/jcr/granite/1.0");
        rootElement.setAttribute("xmlns:cq", "http://www.day.com/jcr/cq/1.0");
        rootElement.setAttribute("xmlns:jcr", "http://www.jcp.org/jcr/1.0");
        rootElement.setAttribute("xmlns:nt", "http://www.jcp.org/jcr/nt/1.0");

        return rootElement;
    }

    /**
     * Method will transform Document structure by prettify xml elements to file.
     *
     * @param document The {@link Document} object
     * @param filePath The path to the file
     */
    public static void transformDomToFile(Document document, String filePath) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer tr = tf.newTransformer();

            //config for beautify/prettify xml content.
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(document);
            File file = CommonUtils.getNewFileAtPathAndRenameExisting(filePath);
            StreamResult result = new StreamResult(file);

            //transform your DOM source to the given file location.
            tr.transform(source, result);

        } catch (Exception e) {
            throw new GeneratorException("Exception while DOM conversion to file : " + filePath, e);
        }
    }
}
