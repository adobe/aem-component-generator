package com.adobe.aem.compgenerator.web;

import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.adobe.aem.compgenerator.models.Options;
import com.adobe.aem.compgenerator.models.Property;
import com.adobe.aem.compgenerator.models.Tab;
import com.adobe.aem.compgenerator.utils.CommonUtils;
import com.adobe.aem.compgenerator.web.model.Message;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.adobe.aem.compgenerator.Constants.*;

public class PropertyBuilderServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(PropertyBuilderServlet.class);

    private Property updateOrCreatePropertyFromRequest(String id, JsonNode reConfig, Property property) {
        if (Objects.isNull(property)) {
            property = new Property();
        }
        property.setId(id);
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
            JsonNode attrArr = reConfig.get("attributes");
            if (attrArr.isArray()) {
                for (final JsonNode objNode : attrArr) {
                    updatedAttributes.put(objNode.get("key").asText(), objNode.get("value").asText());
                }
            }
            property.setAttributes(updatedAttributes);
        }
        return property;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = resp.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Message msg = new Message(true, "Tab builder getter");
        writer.write(mapper.writeValueAsString(msg));
        writer.close();
    }

    @Override
    /*
     * Handles updates to the dialog properties configuration
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        File configFile = new File(CONFIG_PATH);
        // sanity check for config file existence
        if (!configFile.exists()) {
            resp.setStatus(500);
            PrintWriter writer = resp.getWriter();
            Message msg = new Message(false, MISSING_CONFIG_MSG);
            writer.write(mapper.writeValueAsString(msg));
            writer.close();
            return;
        }
        boolean updated = false;
        GenerationConfig config = CommonUtils.getComponentData(configFile);
        Options options = config.getOptions();
        // sanity check on request payload
        if (req.getContentLength() <= 0) {
            resp.setStatus(500);
        } else {
            // get config params as json from request body
            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonNode reConfig = mapper.readTree(body);
            if (reConfig.has("moveProp")) {
                updated = true;
                int oldIndex = reConfig.get("oldIndex").asInt();
                int newIndex = reConfig.get("newIndex").asInt();
                String type = reConfig.get(PROP_TYPE).asText();
                LOG.info("Moving " + type + " property with indexes " + oldIndex + ", " + newIndex);
                switch (type) {
                    case MAIN: {
                        List<Property> existingProperties = options.getProperties();
                        Collections.swap(existingProperties, oldIndex, newIndex);
                        options.setProperties(existingProperties);
                        break;
                    }
                    case SHARED: {
                        List<Property> existingProperties = options.getSharedProperties();
                        Collections.swap(existingProperties, oldIndex, newIndex);
                        options.setSharedProperties(existingProperties);
                        break;
                    }
                    case GLOBAL: {
                        List<Property> existingProperties = options.getGlobalProperties();
                        Collections.swap(existingProperties, oldIndex, newIndex);
                        options.setGlobalProperties(existingProperties);
                        break;
                    }
                }
            } else if (reConfig.has("removeProp")) {
                updated = true;
                String id = reConfig.get("id").asText();
                String field = reConfig.get("field").asText();
                String type = reConfig.get(PROP_TYPE).asText();
                LOG.info("Removing " + type + " dialog property field " + field + " with ID " + id);
                switch (type) {
                    case MAIN: {
                        List<Property> newProps = options.getProperties()
                                .stream()
                                .filter(property -> !property.getId().equals(id))
                                .collect(Collectors.toList());
                        options.setProperties(newProps);
                        // remove the now deleted property from any dialog tabs:
                        List<Tab> updatedTabs = options.getTabProperties();
                        updatedTabs.forEach(tab -> {
                            tab.setFields(tab.getFields().stream().filter(f -> !f.equals(field)).collect(Collectors.toList()));
                        });
                        options.setTabProperties(updatedTabs);
                        break;
                    }
                    case SHARED: {
                        List<Property> newProps = options.getSharedProperties()
                                .stream()
                                .filter(property -> !property.getId().equals(id))
                                .collect(Collectors.toList());
                        options.setSharedProperties(newProps);
                        List<Tab> updatedTabs = options.getSharedTabProperties();
                        updatedTabs.forEach(tab -> {
                            tab.setFields(tab.getFields().stream().filter(f -> !f.equals(field)).collect(Collectors.toList()));
                        });
                        options.setSharedTabProperties(updatedTabs);
                        break;
                    }
                    case GLOBAL: {
                        List<Property> newProps = options.getGlobalProperties()
                                .stream()
                                .filter(property -> !property.getId().equals(id))
                                .collect(Collectors.toList());
                        options.setGlobalProperties(newProps);
                        List<Tab> updatedTabs = options.getGlobalTabProperties();
                        updatedTabs.forEach(tab -> {
                            tab.setFields(tab.getFields().stream().filter(f -> !f.equals(field)).collect(Collectors.toList()));
                        });
                        options.setGlobalTabProperties(updatedTabs);
                        break;
                    }
                }
            } else {
                // else update/new property action
                updated = true;
                // find if this is an existing property to update
                String id = reConfig.get("id").asText();
                String type = reConfig.get(PROP_TYPE).asText();
                LOG.info("Attempting update of " + type + " dialog property with ID " + id);
                switch (type) {
                    case MAIN: {
                        List<Property> existingProperty = options.getProperties()
                                .stream()
                                .filter(property -> property.getId().equals(id))
                                .collect(Collectors.toList());
                        if (existingProperty.isEmpty()) {
                            LOG.info("No main property exists with that id -> creating new one.");
                            List<Property> existingProps = options.getProperties();
                            Property newProp = updateOrCreatePropertyFromRequest(id, reConfig, null);
                            existingProps.add(newProp);
                            options.setProperties(existingProps);
                        } else {
                            // update existing property...
                            updated = true;
                            options.getProperties().forEach(property -> {
                                if (property.getId().equals(id)) {
                                    updateOrCreatePropertyFromRequest(id, reConfig, property);
                                }
                            });
                        }
                        break;
                    }
                    case SHARED: {
                        List<Property> existingProperty = options.getSharedProperties()
                                .stream()
                                .filter(property -> property.getId().equals(id))
                                .collect(Collectors.toList());
                        if (existingProperty.isEmpty()) {
                            LOG.info("No shared property exists with that id -> creating new one.");
                            List<Property> existingProps = options.getSharedProperties();
                            Property newProp = updateOrCreatePropertyFromRequest(id, reConfig, null);
                            existingProps.add(newProp);
                            options.setSharedProperties(existingProps);
                        } else {
                            // update existing property...
                            updated = true;
                            options.getSharedProperties().forEach(property -> {
                                if (property.getId().equals(id)) {
                                    updateOrCreatePropertyFromRequest(id, reConfig, property);
                                }
                            });
                        }
                        break;
                    }
                    case GLOBAL: {
                        List<Property> existingProperty = options.getGlobalProperties()
                                .stream()
                                .filter(property -> property.getId().equals(id))
                                .collect(Collectors.toList());
                        if (existingProperty.isEmpty()) {
                            LOG.info("No global property exists with that id -> creating new one.");
                            List<Property> existingProps = options.getGlobalProperties();
                            Property newProp = updateOrCreatePropertyFromRequest(id, reConfig, null);
                            existingProps.add(newProp);
                            options.setGlobalProperties(existingProps);
                        } else {
                            // update existing property...
                            updated = true;
                            options.getGlobalProperties().forEach(property -> {
                                if (property.getId().equals(id)) {
                                    updateOrCreatePropertyFromRequest(id, reConfig, property);
                                }
                            });
                        }
                        break;
                    }
                }
            }

        }
        config.setOptions(options);

        if (updated) {
            DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
            pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            mapper.writer(pp).writeValue(configFile, config);
        }

        PrintWriter writer = resp.getWriter();
        Message msg = new Message(true, updated ? UPDATED_MSG : NO_UPDATE_MSG);
        writer.write(mapper.writeValueAsString(msg));
        writer.close();
    }

}
