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
package com.adobe.aem.compgenerator;

import com.adobe.aem.compgenerator.exceptions.GeneratorException;
import com.adobe.aem.compgenerator.web.ConfigurationReadWriteServlet;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Root of the AEM Component generator.
 *
 * AemCompGenerator reads the json data file input and creates folder, file
 * structure of an AEM component and sling model interface with member values
 * and getters.
 */
public class AemCompGenerator {
    private static final Logger LOG = LogManager.getLogger(AemCompGenerator.class);

    public static void main(String[] args) {

        // Ensure that an initial data configuration
        // JSON file exists for loading / saving data to
        String configPath = "data-config.json";

        File configFile = new File(configPath);

        if (!configFile.exists()) {
            // if the config does not exist, copy a sample one to the root directory
            LOG.info(configPath + " file does not exist.. creating new empty one from sample file");
            try {
                File file = new File(AemCompGenerator.class.getClassLoader().getResource("data-config-sample.json").getFile());
                Files.copy(file.toPath(), configFile.toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                throw new GeneratorException("Could not initialize data config file");
            }
        }

        DeploymentInfo servletBuilder = deployment()
                .setClassLoader(AemCompGenerator.class.getClassLoader())
                .setContextPath("/api")
                .setDefaultEncoding("UTF-8")
                .setDeploymentName("componentgen.war")
                .addServlets(
                        servlet("global", ConfigurationReadWriteServlet.class)
                                .addMapping("/*"));

        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        try {
            HttpHandler servletHandler = manager.start();
            ResourceHandler pathResourceManager = new ResourceHandler(
                    new PathResourceManager(Paths.get("src/main/resources/static/build"), 100))
                    .setWelcomeFiles("index.html");
            Undertow server = Undertow.builder()
                    .setServerOption(UndertowOptions.URL_CHARSET, "UTF8")
                    .addHttpListener(8080, "localhost")
                    .setHandler(
                            Handlers.path()
                                    .addPrefixPath("/servlet", servletHandler)
                                    .addPrefixPath("/", pathResourceManager)
                                    .addPrefixPath("/config", pathResourceManager)
                                    .addPrefixPath("/builder", pathResourceManager)
                    ).build();
            server.start();
        } catch (ServletException e) {
            e.printStackTrace();
        }
//        try {
//            String configPath = "data-config.json";
//            if (args.length > 0) {
//                configPath = args[0];
//            }
//
//            File configFile = new File(configPath);
//
//            if (CommonUtils.isFileBlank(configFile)) {
//                throw new GeneratorException("Config file missing / empty.");
//            }
//
//            GenerationConfig config = CommonUtils.getComponentData(configFile);
//
//            if (config == null) {
//                throw new GeneratorException("Config file is empty / null !!");
//            }
//
//            if (!config.isValid() || !CommonUtils.isModelValid(config.getProjectSettings())) {
//                throw new GeneratorException("Mandatory fields missing in the data-config.json !");
//            }
//
//            String compDir = config.getProjectSettings().getAppsPath() + "/"
//                    + config.getProjectSettings().getComponentPath() + "/"
//                    + config.getType() + "/" + config.getName();
//            config.setCompDir(compDir);
//
//            //builds component folder and file structure.
//            ComponentUtils generatorUtils = new ComponentUtils(config);
//            generatorUtils.buildComponent();
//
//            //builds sling model based on config.
//            if (config.getOptions() != null && config.getOptions().isHasSlingModel()) {
//                JavaCodeModel javaCodeModel = new JavaCodeModel();
//                javaCodeModel.buildSlingModel(config);
//            }
//        } catch (Exception e) {
//            LOG.error("Failed to generate aem component.", e);
//        }
    }
}
