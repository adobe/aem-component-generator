package com.adobe.aem.compgenerator.web;

import com.adobe.aem.compgenerator.models.*;
import com.adobe.aem.compgenerator.utils.CommonUtils;
import com.adobe.aem.compgenerator.web.model.Message;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class TabBuilderServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(TabBuilderServlet.class);

    private void updateTabProperties(JsonNode reConfig, Tab tab) {
        if (reConfig.has("label")) {
            tab.setLabel(reConfig.get("label").asText());
        }
        if (reConfig.has("fields")) {
            List<String> updatedFields = new ArrayList<>();
            JsonNode val = reConfig.get("fields");
            if (val.isArray()) {
                for (JsonNode jsonNode : val) {
                    String field = jsonNode.get("value").asText();
                    updatedFields.add(field);
                }
            }
            tab.setFields(updatedFields);
        }
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
     * Handles updates to the Dialog Tab configurations
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
            if (reConfig.has("moveTab")) {
                updated = true;
                int oldIndex = reConfig.get("oldIndex").asInt();
                int newIndex = reConfig.get("newIndex").asInt();
                LOG.info("Moving dialog tabs with indexes " + oldIndex + ", " + newIndex);
                List<Tab> existingTabs = options.getTabProperties();
                Collections.swap(existingTabs, oldIndex, newIndex);
                options.setTabProperties(existingTabs);
            } else if (reConfig.has("removeTab")) {
                String id = reConfig.get("id").asText();
                updated = true;
                LOG.info("Remove dialog tab with ID " + id);
                List<Tab> newTabs = options.getTabProperties()
                        .stream()
                        .filter(tab -> !tab.getId().equals(id))
                        .collect(Collectors.toList());
                options.setTabProperties(newTabs);
            } else {
                String id = reConfig.get("id").asText();
                LOG.info("Attempting update of dialog tab with ID " + id);
                List<Tab> existingTab = options.getTabProperties()
                        .stream()
                        .filter(tab -> tab.getId().equals(id))
                        .collect(Collectors.toList());
                // is this a new tab? > create it
                if (existingTab.isEmpty()) {
                    updated = true;
                    LOG.info("no existing tab with that id -> creating new one");
                    List<Tab> existingTabs = options.getTabProperties();
                    Tab tab = new Tab();
                    tab.setId(id);
                    updateTabProperties(reConfig, tab);
                    existingTabs.add(tab);
                    options.setTabProperties(existingTabs);
                } else {
                    updated = true;
                    // update the existing tabs properties...
                    options.getTabProperties().forEach(tab -> {
                        if (tab.getId().equals(id)) {
                            updateTabProperties(reConfig, tab);
                        }
                    });
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
