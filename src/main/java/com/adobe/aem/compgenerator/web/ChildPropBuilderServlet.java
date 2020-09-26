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
import com.fasterxml.jackson.databind.annotation.JsonAppend;
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

public class ChildPropBuilderServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(ChildPropBuilderServlet.class);

    private void updatePropertyFromRequest(JsonNode reConfig, Property property) {
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
    }

    private int getIndexOfPropertyById(List<Property> properties, String id) {
        int index = -1;
        for (int i = 0; i < properties.size(); i++) {
            if (properties.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private List<Property> getExistingChildPropertyFromConfig(List<Property> properties, String id, String parentId) {
        List<Property> existingProperty = properties
                .stream()
                .filter(property -> property.getId().equals(parentId))
                .collect(Collectors.toList());
        if (existingProperty.isEmpty()) {
            return new ArrayList<>();
        } else {
            if (Objects.isNull(existingProperty.get(0).getItems())) {
                return new ArrayList<>();
            } else {
                return existingProperty.get(0).getItems()
                        .stream()
                        .filter(property -> property.getId().equals(id))
                        .collect(Collectors.toList());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = resp.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Message msg = new Message(true, "Child property builder getter");
        writer.write(mapper.writeValueAsString(msg));
        writer.close();
    }

    @Override
    /*
     * Handles updates to the child items of properties
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
                String propertyId = reConfig.get("propertyId").asText();
                int oldIndex = reConfig.get("oldIndex").asInt();
                int newIndex = reConfig.get("newIndex").asInt();
                LOG.info("Moving child property with indexes " + oldIndex + ", " + newIndex);
                int index = getIndexOfPropertyById(options.getProperties(), propertyId);
                List<Property> existingProperties = options.getProperties().get(index).getItems();
                Collections.swap(existingProperties, oldIndex, newIndex);
                options.getProperties().get(index).setItems(existingProperties);
            } else if (reConfig.has("removeProp")) {
                String id = reConfig.get("id").asText();
                String propertyId = reConfig.get("propertyId").asText();
                updated = true;
                LOG.info("Removing child prop with ID " + id);
                int index = getIndexOfPropertyById(options.getProperties(), propertyId);
                List<Property> newProps = options.getProperties().get(index).getItems()
                        .stream()
                        .filter(property -> !property.getId().equals(id))
                        .collect(Collectors.toList());
                options.getProperties().get(index).setItems(newProps);
            } else {
                String id = reConfig.get("id").asText();
                String propertyId = reConfig.get("propertyId").asText();
                LOG.info("Attempting update of child property with ID " + id);
                List<Property> existingPropertyList = getExistingChildPropertyFromConfig(options.getProperties(), id, propertyId);
                // is this a new child property? -> create it
                if (existingPropertyList.isEmpty()) {
                    updated = true;
                    LOG.info("No existing child property exists with that id -> creating new one.");
                    Property newProp = new Property();
                    newProp.setId(id);
                    updatePropertyFromRequest(reConfig, newProp);
                    int index = getIndexOfPropertyById(options.getProperties(), propertyId);
                    List<Property> newPropList = new ArrayList<>();
                    newPropList.add(newProp);
                    options.getProperties().get(index).setItems(newPropList);
                } else {
                    updated = true;
                    // update the existing tabs properties...
                    int index = getIndexOfPropertyById(options.getProperties(), propertyId);
                    int childIndex = getIndexOfPropertyById(options.getProperties().get(index).getItems(), id);
                    updatePropertyFromRequest(reConfig, options.getProperties().get(index).getItems().get(childIndex));
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
