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
import com.adobe.aem.compgenerator.models.BaseModel;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommonUtils {

    private static final Logger LOG = LogManager.getLogger(CommonUtils.class);
    private static final Date CURRENT_TIME = new Date(System.currentTimeMillis());

    /**
     * Method to map JSON content from given file into given GenerationConfig type.
     *
     * @param jsonDataFile data-config file
     * @return GenerationConfig java class with the mapped content in json file
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
     * Renames the file at the given path (if it exists) and returns a new File with the given path.
     *
     * @param path file path
     * @return File with the given path
     * @throws IOException exception
     */
    public static File getNewFileAtPathAndRenameExisting(String path) throws IOException{
        File file = new File(path);
        if (file.exists()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.RENAME_FILE_DATE_PATTERN);
            String date = simpleDateFormat.format(CURRENT_TIME);
            File oldFile = new File(path + ".sv." + date);

            boolean isSuccess = file.renameTo(oldFile);
            if (isSuccess) {
                LOG.info("Replaced: " + path + " (Old file: " + oldFile.getName() + ")");
                return file;
            } else {
                throw new IOException();
            }
        }

        LOG.info("Created: " + path);
        return file;
    }

    /**
     * Method checks if the file exists and not empty.
     *
     * @param file file to check.
     * @return boolean return true when file not exists or length is zero.
     */
    public static boolean isFileBlank(File file) {
        return file.exists() && file.length() != 0 ? false : true;
    }

    /**
     * Method to read the content of the provided template file as string.
     *
     * @param filePath Path to the template file in the project
     * @param stringsToReplaceValueMap The string values to replace in the template string
     * @return String return content of the resource file as string or null when file not exists
     */
    public static String getTemplateFileAsString(String filePath, Map<String, String> stringsToReplaceValueMap) {
        try (InputStream inputStream = CommonUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringSubstitutor stringSubstitutor = new StringSubstitutor(stringsToReplaceValueMap);
            String content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            return stringSubstitutor.replace(content);
        } catch (IOException e) {
            LOG.error("Failed to read " + filePath + " from the classpath.", e);
        }
        return null;
    }
    /**
     * Creates a new folder.
     *
     * @param folderPath The path where the folder gets created
     * @return Path
     * @throws Exception exception
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
     *
     * @param model The {@link BaseModel} object
     * @return boolean
     */
    public static boolean isModelValid(BaseModel model) {
        return model != null && model.isValid();
    }

    /**
     * Creates a new file with the correct copyright text appearing at the top.
     *
     * @param path Full path including the new file name
     * @param stringsToReplaceValueMap The string values to replace in the template string
     * @throws IOException exception
     */
    public static void createFileWithCopyRight(String path, Map<String, String> stringsToReplaceValueMap) throws IOException {
        String template = Constants.TEMPLATE_COPYRIGHT_JAVA;
        if (path.endsWith("js") || path.endsWith("java")) {
            template = Constants.TEMPLATE_COPYRIGHT_JAVA;
        } else if (path.endsWith("less")) {
            template = Constants.TEMPLATE_COPYRIGHT_CSS;
        } else if (path.endsWith("xml")) {
            template = Constants.TEMPLATE_COPYRIGHT_XML;
        } else if (path.endsWith("html")) {
            template = Constants.TEMPLATE_COPYRIGHT_HTL;
        }

        BufferedWriter writer = getFileWriterFromTemplate(path, template, stringsToReplaceValueMap);
        writer.close();
    }

    /**
     * Creates the css.txt or js.txt file for a clientLib.
     *
     * @param path Full path including the new file name
     * @param stringsToReplaceValueMap The string values to replace in the template string
     * @param clientLibFileName The less/js file's name
     * @throws IOException exception
     */
    public static void createClientlibTextFile(String path,
            Map<String, String> stringsToReplaceValueMap, String clientLibFileName) throws IOException {

        BufferedWriter writer = getFileWriterFromTemplate(path, Constants.TEMPLATE_COPYRIGHT_TEXT, stringsToReplaceValueMap);
        writer.newLine();

        if (path.endsWith("js.txt")) {
            writer.write("#base=js");
        }

        if (path.endsWith("css.txt")) {
            writer.write("#base=css");
        }

        writer.newLine();
        writer.newLine();
        writer.write(clientLibFileName);
        writer.close();
    }

    /**
     * Creates a map of strings to replace placeholder values on template files.
     *
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     * @return Map<String, String>
     */
    public static Map<String, String> getStringsToReplaceValueMap(GenerationConfig generationConfig) {
        if (generationConfig != null) {
            Map<String, String> map = new HashMap<>();
            map.put("name", generationConfig.getName());
            map.put("title", generationConfig.getTitle());
            map.put("sightly", StringUtils.uncapitalize(generationConfig.getJavaFormatedName()));
            map.put("slingModel", generationConfig.getProjectSettings().getModelInterfacePackage() + "." + generationConfig.getJavaFormatedName());
            map.put("CODEOWNER", generationConfig.getProjectSettings().getCodeOwner());
            return map;
        }
        return null;
    }

    /**
     * Construct a resource type from the {@link GenerationConfig} object.
     *
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     * @return String
     */
    public static String getResourceType(GenerationConfig generationConfig) {
        return generationConfig.getProjectSettings().getComponentPath() + "/"
                + generationConfig.getType() + "/" + generationConfig.getName();
    }

    /**
     * Creates a {@link BufferedWriter} from the provided 'template'.
     *
     * @param path Full path including the new file name
     * @param template The template to use when creating the {@link BufferedWriter}
     * @param stringsToReplaceValueMap The string values to replace in the template string
     * @throws IOException exception
     */
    private static BufferedWriter getFileWriterFromTemplate(String path,
            String template, Map<String, String> stringsToReplaceValueMap) throws IOException {

        File file = getNewFileAtPathAndRenameExisting(path);
        String templateString = getTemplateFileAsString(template, stringsToReplaceValueMap);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(templateString);
        return writer;
    }
}
