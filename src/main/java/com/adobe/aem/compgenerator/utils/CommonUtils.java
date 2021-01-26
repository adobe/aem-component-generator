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
import com.adobe.aem.compgenerator.models.Property;
import com.adobe.aem.compgenerator.models.Tab;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
                throw new GeneratorException(String.format("Exception while reading config file. %n %s", e.getMessage()), e);
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
    public static File getNewFileAtPathAndRenameExisting(String path) throws IOException {
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
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     * @return String return content of the resource file as string or null when file not exists
     */
    public static String getTemplateFileAsString(String filePath, GenerationConfig generationConfig) {
        try (InputStream inputStream = CommonUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            Map<String, String> stringsToReplaceValueMap = getStringsToReplaceValueMap(generationConfig);
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
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     * @throws IOException exception
     */
    public static void createFileWithCopyRight(String path, GenerationConfig generationConfig) throws IOException {
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

        BufferedWriter writer = getFileWriterFromTemplate(path, template, generationConfig);
        writer.close();
    }

    /**
     * Creates the css.txt or js.txt file for a clientLib.
     *
     * @param path Full path including the new file name
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     * @param clientLibFileName The less/js file's name
     * @throws IOException exception
     */
    public static void createClientlibTextFile(String path,
                                               GenerationConfig generationConfig, String clientLibFileName) throws IOException {

        BufferedWriter writer = getFileWriterFromTemplate(path, Constants.TEMPLATE_COPYRIGHT_TEXT, generationConfig);
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
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     * @throws IOException exception
     */
    private static BufferedWriter getFileWriterFromTemplate(String path,
                                                            String template, GenerationConfig generationConfig) throws IOException {

        File file = getNewFileAtPathAndRenameExisting(path);
        String templateString = getTemplateFileAsString(template, generationConfig);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(templateString);
        return writer;
    }

    /**
     * Creates a map of strings to replace placeholder values on template files.
     *
     * @param generationConfig The {@link GenerationConfig} object with all the populated values
     * @return Map<String, String>
     */
    private static Map<String, String> getStringsToReplaceValueMap(GenerationConfig generationConfig) {
        if (generationConfig != null) {
            Map<String, String> map = new HashMap<>();
            map.put("name", generationConfig.getName());
            map.put("title", generationConfig.getTitle());
            map.put("sightly", StringUtils.uncapitalize(generationConfig.getJavaFormatedName()));
            map.put("slingModel", generationConfig.getProjectSettings().getModelInterfacePackage() + "." + generationConfig.getJavaFormatedName());
            map.put("CODEOWNER", generationConfig.getProjectSettings().getCodeOwner());
            String year = generationConfig.getProjectSettings().getYear();
            if (StringUtils.isNotEmpty(year) && year.equals("current")) {
                year = String.valueOf(Year.now().getValue());
            } else if (StringUtils.isEmpty(year)) {
                year = String.valueOf(Year.now().getValue());
            }
            map.put("YEAR", year);
            map.put("htmlOutput", generationConfig.getOptions().isHtmlContent() ? HTMLUtils.renderHtml(generationConfig) : "    <!-- Component HTML goes here -->");
            return map;
        }
        return null;
    }


    /**
     * Gets the Sorted properties based on tabs. If the tabs are not present, all properties will be considered.
     *
     * @param properties The {@link Property}
     * @param tabs The {@link Tab}
     * @return List<Property>
     */
    public static List<Property> getSortedPropertiesBasedOnTabs(List<Property> properties, List<Tab> tabs) {
        List<Property> updatedPropertiesList = null;
        if (null != tabs && !tabs.isEmpty()) {
            List<Property> sortedProperties = new ArrayList<>();
            Map<String, Property> propertiesMap = properties.stream()
                    .collect(Collectors.toMap(Property::getField, Function.identity()));
            for (Tab tab : tabs) {
                sortedProperties.addAll(tab.getFields().stream().map(propertiesMap::get).collect(Collectors.toList()));
            }
            updatedPropertiesList = sortedProperties;
        } else {
            updatedPropertiesList = properties;
        }
        return updatedPropertiesList;
    }

    /**
     * Returns the jar file used to load class clazz, or defaultJar if clazz was not loaded from a
     * jar.
     */
    public static JarFile jarForClass(Class<?> clazz, JarFile defaultJar) {
        String path = "/" + clazz.getName().replace('.', '/') + ".class";
        URL jarUrl = clazz.getResource(path);
        if (jarUrl == null) {
            return defaultJar;
        }

        String url = jarUrl.toString();
        int bang = url.indexOf("!");
        String JAR_URI_PREFIX = "jar:file:";
        if (url.startsWith(JAR_URI_PREFIX) && bang != -1) {
            try {
                return new JarFile(url.substring(JAR_URI_PREFIX.length(), bang));
            } catch (IOException e) {
                throw new IllegalStateException("Error loading jar file.", e);
            }
        } else {
            return defaultJar;
        }
    }

    /**
     * Copies a directory from a jar file to an external directory.
     */
    public static void copyResourcesToDirectory(JarFile fromJar, String jarDir, String destDir)
            throws IOException {
        for (Enumeration<JarEntry> entries = fromJar.entries(); entries.hasMoreElements();) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(jarDir + "/") && !entry.isDirectory()) {
                File dest = new File(destDir + "/" + entry.getName().substring(jarDir.length() + 1));
                File parent = dest.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                FileOutputStream out = new FileOutputStream(dest);
                InputStream in = fromJar.getInputStream(entry);

                try {
                    byte[] buffer = new byte[8 * 1024];

                    int s = 0;
                    while ((s = in.read(buffer)) > 0) {
                        out.write(buffer, 0, s);
                    }
                } catch (IOException e) {
                    throw new IOException("Could not copy asset from jar file", e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException ignored) {}
                    try {
                        out.close();
                    } catch (IOException ignored) {}
                }
            }
        }

    }
}
