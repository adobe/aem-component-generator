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
import com.bounteous.aem.compgenerator.models.BaseModel;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.Map;
import java.util.stream.Collectors;

public class CommonUtils {
    private static final Logger LOG = LogManager.getLogger(CommonUtils.class);

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
     * method checks if the file exists and not empty.
     *
     * @param file file to check.
     * @return boolean return true when file not exists or length is zero.
     */
    public static boolean isFileBlank(File file) {
        return file.exists() && file.length() != 0 ? false : true;
    }

    /**
     * method to read the content of any resource file in the project as string.
     *
     * @param filePath path to the resource file in project.
     * @return string return content of the resource file as string or null when file not exists.
     */
    public static String getResourceContentAsString(String filePath) {
        try (InputStream inputStream = CommonUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            LOG.error("Failed to read " + filePath + " from the classpath.", e);
        }
        return null;
    }
    /**
     * Creates a new folder
     * @param folderPath
     * @return
     * @throws Exception
     */
    public static Path createFolder(String folderPath) throws Exception {
        Path path = Paths.get(folderPath);
        if (Files.notExists(path)) {
            return Files.createDirectories(path);
        }
        return path;
    }

    /**
     * Determines if the model included is valid and not null.
     * @param model
     * @return
     */
    public static boolean isModelValid(BaseModel model) {
        return model != null && model.isValid();
    }

    /**
     * Creates a new file with the correct copyright text appearing at the top.
     * @param path
     * @param templateValueMap
     * @throws IOException
     */
    public static void createFileWithCopyRight(String path, Map<String, String> templateValueMap) throws IOException {
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

            StrSubstitutor strSubstitutor = new StrSubstitutor(templateValueMap);
            String templateString = CommonUtils.getResourceContentAsString(template);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(strSubstitutor.replace(templateString));
            writer.close();
        }
    }

    /**
     * creates a map of values required for any template. Let's say htl template and others if any.
     *
     * @return map
     */
    public static Map<String, String> getTemplateValueMap(GenerationConfig generationConfig) {
        if (generationConfig != null) {
            Map<String, String> map = new HashMap<>();
            map.put("name", generationConfig.getName());
            map.put("title", generationConfig.getTitle());
            map.put("sightly", StringUtils.uncapitalize(generationConfig.getJavaFormatedName()));
            map.put("slingModel", generationConfig.getProjectSettings().getModelInterfacePackage() + "." + generationConfig.getJavaFormatedName());
            return map;
        }
        return null;
    }
}
