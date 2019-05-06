package com.bounteous.aem.compgenerator.utils;

import com.bounteous.aem.compgenerator.Constants;
import com.bounteous.aem.compgenerator.exceptions.GeneratorException;
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
     * creates root node with of dialog xml with required name spaces as attr.
     *
     * @param document
     * @return
     */
    public static Element createRootElement(Document document) {
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
     * method will transform Document structure by prettify xml elements to file.
     *
     * @param document
     * @param filePath
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
            StreamResult result = new StreamResult(new File(filePath));

            //transform your DOM source to the given file location.
            tr.transform(source, result);

        } catch (Exception e) {
            throw new GeneratorException("Exception while DOM conversion to file : " + filePath);
        }
    }
}
