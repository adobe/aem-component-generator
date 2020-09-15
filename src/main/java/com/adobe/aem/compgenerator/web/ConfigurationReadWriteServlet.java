package com.adobe.aem.compgenerator.web;

import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.adobe.aem.compgenerator.models.ProjectSettings;
import com.adobe.aem.compgenerator.utils.CommonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet("/global")
public class ConfigurationReadWriteServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(ConfigurationReadWriteServlet.class);
    public static final String CONFIG_PATH = "data-config.json";

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
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
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter writer = resp.getWriter();
        String msg = "{ \"message\" : \"" + "PUT!!!" + "\" }";
        writer.write(msg);
        writer.close();
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        boolean updated = false;
        File configFile = new File(CONFIG_PATH);
        GenerationConfig config = CommonUtils.getComponentData(configFile);
        ProjectSettings projectSettings = config.getProjectSettings();
        // get params as json from request
        String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode reConfig = mapper.readTree(body);

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

        config.setProjectSettings(projectSettings);

        String compDir = projectSettings.getAppsPath() + "/"
                + projectSettings.getComponentPath() + "/"
                + config.getType() + "/" + config.getName();
        config.setCompDir(compDir);

        if (updated) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
        }

        PrintWriter writer = resp.getWriter();
        String msg = "{ \"message\" : \"" + (updated ? UPDATED_MSG : NO_UPDATE_MSG) + "\" }";
        writer.write(msg);
        writer.close();
    }
}
