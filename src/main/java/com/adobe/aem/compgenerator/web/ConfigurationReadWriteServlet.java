package com.adobe.aem.compgenerator.web;

import acscommons.com.google.common.primitives.Ints;
import com.adobe.aem.compgenerator.javacodemodel.JavaCodeModel;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.adobe.aem.compgenerator.models.Options;
import com.adobe.aem.compgenerator.models.ProjectSettings;
import com.adobe.aem.compgenerator.models.Property;
import com.adobe.aem.compgenerator.utils.CommonUtils;
import com.adobe.aem.compgenerator.utils.ComponentUtils;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.adobe.aem.compgenerator.Constants.NO_UPDATE_MSG;
import static com.adobe.aem.compgenerator.Constants.UPDATED_MSG;

/**
 * ConfigurationReadWriteServlet
 *
 * contains the methods to update the various
 * fields within the GenerationConfig class
 * and to generate the code on demand from the web UI
 *
 */
public class ConfigurationReadWriteServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(ConfigurationReadWriteServlet.class);
    public static final String CONFIG_PATH = "data-config.json";

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    /*
     * Handles retrieving the current state of the configuration json
     */
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        File configFile = new File(CONFIG_PATH);
        GenerationConfig config = CommonUtils.getComponentData(configFile);

        ObjectMapper mapper = new ObjectMapper();

        PrintWriter writer = resp.getWriter();
        writer.write(mapper.writeValueAsString(config));
        writer.close();
    }

    @Override
    /*
     * Handles resetting the config file to a default state
     */
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    /*
     *  Handles generating the code from a complete GenerationConfig instance
     */
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        File configFile = new File(CONFIG_PATH);
        GenerationConfig config = CommonUtils.getComponentData(configFile);

        if (!config.isValid() || !CommonUtils.isModelValid(config.getProjectSettings())) {
            resp.setStatus(500);
            PrintWriter writer = resp.getWriter();
            String msg = "{ \"result\": false, \"message\" : \"" + "Validation of config file failed, required fields are missing." + "\" }";
            writer.write(msg);
            writer.close();
        } else {
            String compDir = config.getProjectSettings().getAppsPath() + "/"
                    + config.getProjectSettings().getComponentPath() + "/"
                    + config.getType() + "/" + config.getName();
            config.setCompDir(compDir);

            ComponentUtils generatorUtils = new ComponentUtils(config);
            try {
                generatorUtils.buildComponent();
                //builds sling model based on config.
                if (config.getOptions() != null && config.getOptions().isHasSlingModel()) {
                    JavaCodeModel javaCodeModel = new JavaCodeModel();
                    javaCodeModel.buildSlingModel(config);
                }
            } catch (Exception e) {
                resp.setStatus(500);
                PrintWriter writer = resp.getWriter();
                String msg = "{ \"result\": false, \"message\" : \"" + "Validation of config file failed, required fields are missing." + "\" }";
                writer.write(msg);
                writer.close();
            }

            PrintWriter writer = resp.getWriter();
            String msg = "{ \"result\": true, \"message\" : \"" + "Code generated successfully" + "\" }";
            writer.write(msg);
            writer.close();
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        boolean updated = false;
        File configFile = new File(CONFIG_PATH);
        GenerationConfig config = CommonUtils.getComponentData(configFile);
        ProjectSettings projectSettings = config.getProjectSettings();
        Options options = config.getOptions();
        if (req.getContentLength() <= 0) {
            resp.setStatus(500);
        } else {
            // get config params as json from request body
            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode reConfig = mapper.readTree(body);

            if (reConfig.has("removeProp")) {
                updated = true;
                String id = reConfig.get("id").asText();
                LOG.info("Remove dialog property with ID " + id);
                List<Property> newProps = options.getProperties()
                        .stream()
                        .filter(property -> !property.getId().equals(id))
                        .collect(Collectors.toList());
                options.setProperties(newProps);
            }
            // if this is an updated property handle it
            else if (reConfig.has("updateProp")) {
                updated = true;
                // find if existing property to update
                String id = reConfig.get("id").asText();
                LOG.info("Attempting update of dialog property with ID " + id);
                List<Property> existingProperty = options.getProperties()
                        .stream()
                        .filter(property -> property.getId().equals(id))
                        .collect(Collectors.toList());
                if (existingProperty.isEmpty()) {
                    LOG.info("no existing property with that id -> creating new one");
                    List<Property> existingProps = options.getProperties();
                    Property newProp = new Property();
                    newProp.setId(id);
                    if (reConfig.has("field")) {
                        newProp.setField(reConfig.get("field").asText());
                    }
                    if (reConfig.has("label") && !reConfig.get("label").isNull()) {
                        newProp.setLabel(reConfig.get("label").asText());
                    }
                    if (reConfig.has("description") && !reConfig.get("description").isNull()) {
                        if (StringUtils.isNotEmpty(reConfig.get("description").asText())) {
                            newProp.setDescription(reConfig.get("description").asText());
                        }
                    }
                    if (reConfig.has("javadoc") && !reConfig.get("javadoc").isNull()) {
                        newProp.setJavadoc(reConfig.get("javadoc").asText());
                    }
                    if (reConfig.has("jsonExpose") && !reConfig.get("jsonExpose").isNull()) {
                        newProp.setShouldExporterExpose(reConfig.get("jsonExpose").asBoolean());
                    }
                    if (reConfig.has("useExistingModel")) {
                        newProp.setUseExistingModel(reConfig.get("useExistingModel").asBoolean());
                    }
                    if (reConfig.has("jsonProperty")) {
                        newProp.setJsonProperty(reConfig.get("jsonProperty").asText());
                    }
                    if (reConfig.has("modelName")) {
                        newProp.setModelName(reConfig.get("modelName").asText());
                    }
                    if (reConfig.has("type")) {
                        JsonNode type = reConfig.get("type");
                        String value = type.get("value").textValue();
                        newProp.setType(value);
                    }
                    if (reConfig.has("attributes")) {
                        Map<String, String> updatedAttributes = new HashMap<>();
                        Iterator<Map.Entry<String, JsonNode>> attribsIter = reConfig.get("attributes").fields();
                        while (attribsIter.hasNext()) {
                            Map.Entry<String, JsonNode> entry = attribsIter.next();
                            updatedAttributes.put(entry.getKey(), entry.getValue().textValue());
                        }
                        newProp.setAttributes(updatedAttributes);
                    }
                    // TODO add items saving
                    if (reConfig.has("items")) {
                        LOG.info("properties attempting to save updated items: ", reConfig.get("items"));
                    } else {
                        newProp.setItems(new ArrayList<>());
                    }
                    existingProps.add(newProp);
                    options.setProperties(existingProps);
                } else {
                    updated = true;
                    options.getProperties().forEach(property -> {
                        if (property.getId().equals(id)) {
                            if (reConfig.has("field")) {
                                property.setField(reConfig.get("field").asText());
                            }
                            if (reConfig.has("label") && !reConfig.get("label").isNull()) {
                                property.setLabel(reConfig.get("label").asText());
                            }
                            if (reConfig.has("description") && !reConfig.get("description").isNull()) {
                                if (StringUtils.isNotEmpty(reConfig.get("description").asText())) {
                                    property.setDescription(reConfig.get("description").asText());
                                }
                            }
                            if (reConfig.has("javadoc") && !reConfig.get("javadoc").isNull()) {
                                property.setJavadoc(reConfig.get("javadoc").asText());
                            }
                            if (reConfig.has("jsonExpose") && !reConfig.get("jsonExpose").isNull()) {
                                property.setShouldExporterExpose(reConfig.get("jsonExpose").asBoolean());
                            }
                            if (reConfig.has("useExistingModel")) {
                                property.setUseExistingModel(reConfig.get("useExistingModel").asBoolean());
                            }
                            if (reConfig.has("jsonProperty")) {
                                property.setJsonProperty(reConfig.get("jsonProperty").asText());
                            }
                            if (reConfig.has("modelName")) {
                                property.setModelName(reConfig.get("modelName").asText());
                            }
                            if (reConfig.has("type")) {
                                JsonNode type = reConfig.get("type");
                                String value = type.get("value").textValue();
                                property.setType(value);
                            }
                            if (reConfig.has("attributes")) {
                                Map<String, String> updatedAttributes = new HashMap<>();
                                Iterator<Map.Entry<String, JsonNode>> attribsIter = reConfig.get("attributes").fields();
                                while (attribsIter.hasNext()) {
                                    Map.Entry<String, JsonNode> entry = attribsIter.next();
                                    updatedAttributes.put(entry.getKey(), entry.getValue().textValue());
                                }
                                property.setAttributes(updatedAttributes);
                            }
                            // TODO add items saving
                            if (reConfig.has("items")) {
                                LOG.info("properties attempting to save updated items: ", reConfig.get("items"));
                            }
                        }
                    });
                }
            }

            if (reConfig.has("codeOwner")) {
                updated = true;
                String codeOwner = reConfig.get("codeOwner").textValue();
                projectSettings.setCodeOwner(codeOwner);
            }
            if (reConfig.has("bundlePath")) {
                updated = true;
                String val = reConfig.get("bundlePath").textValue();
                projectSettings.setBundlePath(val);
            }
            if (reConfig.has("testPath")) {
                updated = true;
                String val = reConfig.get("testPath").textValue();
                projectSettings.setTestPath(val);
            }
            if (reConfig.has("appsPath")) {
                updated = true;
                String val = reConfig.get("appsPath").textValue();
                projectSettings.setAppsPath(val);
            }
            if (reConfig.has("componentPath")) {
                updated = true;
                String val = reConfig.get("componentPath").textValue();
                projectSettings.setComponentPath(val);
            }
            if (reConfig.has("modelInterfacePackage")) {
                updated = true;
                String val = reConfig.get("modelInterfacePackage").textValue();
                projectSettings.setModelInterfacePackage(val);
            }
            if (reConfig.has("modelImplPackage")) {
                updated = true;
                String val = reConfig.get("modelImplPackage").textValue();
                projectSettings.setModelImplPackage(val);
            }
            if (reConfig.has("copyrightYear")) {
                updated = true;
                String val = reConfig.get("copyrightYear").textValue();
                projectSettings.setYear(val);
            }
            if (reConfig.has("componentTitle")) {
                updated = true;
                String val = reConfig.get("componentTitle").textValue();
                config.setTitle(val);
            }
            if (reConfig.has("componentNodeName")) {
                updated = true;
                String val = reConfig.get("componentNodeName").textValue();
                config.setName(val);
            }
            if (reConfig.has("componentGroup")) {
                updated = true;
                String val = reConfig.get("componentGroup").textValue();
                config.setGroup(val);
            }
            if (reConfig.has("componentType")) {
                updated = true;
                String val = reConfig.get("componentType").textValue();
                config.setType(val);
            }
            if (reConfig.has("modelAdapters")) {
                updated = true;
                JsonNode val = reConfig.get("modelAdapters");
                ArrayList<String> adapters = new ArrayList<>();
                if (val.isArray()) {
                    for (JsonNode jsonNode : val) {
                        String adaptable = jsonNode.get("value").asText();
                        adapters.add(adaptable);
                    }
                }
                options.setModelAdaptables(adapters.toArray(new String[adapters.size()]));
            }
            if (reConfig.has("js")) {
                updated = true;
                boolean val = reConfig.get("js").booleanValue();
                options.setHasJs(val);
            }
            if (reConfig.has("jsTxt")) {
                updated = true;
                boolean val = reConfig.get("jsTxt").booleanValue();
                options.setHasJsTxt(val);
            }
            if (reConfig.has("css")) {
                updated = true;
                boolean val = reConfig.get("css").booleanValue();
                options.setHasCss(val);
            }
            if (reConfig.has("cssTxt")) {
                updated = true;
                boolean val = reConfig.get("cssTxt").booleanValue();
                options.setHasCssTxt(val);
            }
            if (reConfig.has("html")) {
                updated = true;
                boolean val = reConfig.get("html").booleanValue();
                options.setHasHtml(val);
            }
            if (reConfig.has("htmlContent")) {
                updated = true;
                boolean val = reConfig.get("htmlContent").booleanValue();
                options.setHtmlContent(val);
            }
            if (reConfig.has("slingModel")) {
                updated = true;
                boolean val = reConfig.get("slingModel").booleanValue();
                options.setHasSlingModel(val);
            }
            if (reConfig.has("testClass")) {
                updated = true;
                boolean val = reConfig.get("testClass").booleanValue();
                options.setHasTestClass(val);
            }
            if (reConfig.has("contentExporter")) {
                updated = true;
                boolean val = reConfig.get("contentExporter").booleanValue();
                options.setAllowExporting(val);
            }
            if (reConfig.has("genericJavadoc")) {
                updated = true;
                boolean val = reConfig.get("genericJavadoc").booleanValue();
                options.setHasGenericJavadoc(val);
            }
            if (reConfig.has("junitMajorVersion")) {
                updated = true;
                JsonNode jsonNode = reConfig.get("junitMajorVersion");
                if (jsonNode.isInt()) {
                    options.setJunitVersion(jsonNode.asInt());
                } else {
                    String val = reConfig.get("junitMajorVersion").textValue();
                    if (Objects.nonNull(val)) {
                        Integer valInt = Ints.tryParse(val);
                        options.setJunitVersion(Objects.nonNull(valInt) ? valInt : 5);
                    }
                }
            }
            config.setProjectSettings(projectSettings);
            config.setOptions(options);

            String compDir = projectSettings.getAppsPath() + "/"
                    + projectSettings.getComponentPath() + "/"
                    + config.getType() + "/" + config.getName();
            config.setCompDir(compDir);

            if (updated) {
                DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
                pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
                mapper.writer(pp).writeValue(configFile, config);
            }

        }

        PrintWriter writer = resp.getWriter();
        String msg = "{ \"message\" : \"" + (updated ? UPDATED_MSG : NO_UPDATE_MSG) + "\" }";
        writer.write(msg);
        writer.close();
    }
}
