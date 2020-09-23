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
import com.adobe.aem.compgenerator.utils.CommonUtils;
import com.adobe.aem.compgenerator.web.ConfigurationReadWriteServlet;
import com.adobe.aem.compgenerator.web.TabBuilderServlet;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.jar.JarFile;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

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
        // by default web app will run on port 8080
        int port = 8080;
        // optional ability to override default http port
        if (args.length > 0) {
            String portOption = args[0];
            if (StringUtils.contains(portOption, "p") &&
                    StringUtils.contains(portOption, "=")) {
                String[] portArr = StringUtils.split(portOption, "=");
                try {
                    port = Integer.parseInt(portArr[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid arg usage, expected -p=8080");
                    System.exit(1);
                }
            } else {
                System.out.println("Invalid arg usage, expected -p=8080");
            }
        }
        // Ensure that an initial data configuration
        // JSON file exists for loading / saving data to
        String configPath = "data-config.json";
        String configSamplePath = "data-config-empty.json";

        File configFile = new File(configPath);

        if (!configFile.exists()) {
            // if the config does not exist, copy a sample one to the root directory
            LOG.info(configPath + " file does not exist.. creating new empty one from sample file");
            try {
                InputStream input = AemCompGenerator.class.getResourceAsStream("/resources/" + configSamplePath);
                if (input == null) {
                    input = AemCompGenerator.class.getClassLoader().getResourceAsStream(configSamplePath);
                }
                byte[] buffer = new byte[input.available()];
                input.read(buffer);

                OutputStream outStream = new FileOutputStream(configPath);
                outStream.write(buffer);
            } catch (IOException e) {
                LOG.error(e);
                throw new GeneratorException("Could not initialize data config file");
            }
        }

        DeploymentInfo servletBuilder = deployment()
                .setClassLoader(AemCompGenerator.class.getClassLoader())
                .setContextPath("/api")
                .setDefaultEncoding("UTF-8")
                .setDeploymentName("componentgen.war")
                .addServlet((Servlets.servlet("global", ConfigurationReadWriteServlet.class)
                        .addMapping("/global")))
                .addServlet((Servlets.servlet("tabs", TabBuilderServlet.class)
                        .addMapping("/tabs")));;

        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        try {
            //HttpHandler servletHandler = manager.start();
            PathHandler servletHandler = Handlers.path(Handlers.redirect("/api"))
                    .addPrefixPath("/api", manager.start());
            Path staticPath = Paths.get("src/main/resources/static/build");

            String protocol = AemCompGenerator.class.getResource("").getProtocol();
            // if we are running the app from a Jar file... extract the static web dir
            if (Objects.equals(protocol, "jar")) {
                try {
                    Path tempDirWithPrefix = Files.createTempDirectory("comp_gen_web");
                    tempDirWithPrefix.toFile().deleteOnExit(); // cleanup temp folder when exiting app
                    JarFile jarFile = CommonUtils.jarForClass(AemCompGenerator.class, null);
                    CommonUtils.copyResourcesToDirectory(jarFile, "static/build", tempDirWithPrefix.toAbsolutePath().toString());
                    staticPath = Paths.get(tempDirWithPrefix.toAbsolutePath().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                LOG.info("running in IDE / copying static files from Jar is not needed");
            }

            ResourceHandler pathResourceManager = new ResourceHandler(
                    new PathResourceManager(staticPath, 100))
                    .setWelcomeFiles("index.html");
            Undertow server = Undertow.builder()
                    .setServerOption(UndertowOptions.URL_CHARSET, "UTF8")
                    .addHttpListener(port, "localhost")
                    .setHandler(
                            Handlers.path()
                                    .addPrefixPath("/servlet", servletHandler)
                                    .addPrefixPath("/", pathResourceManager)
                                    .addPrefixPath("/config", pathResourceManager)
                                    .addPrefixPath("/comp-config", pathResourceManager)
                                    .addPrefixPath("/dialog-properties", pathResourceManager)
                                    .addPrefixPath("/dialog-tabs", pathResourceManager)
                    ).build();
            server.start();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                System.out.println("Launching new browser window with Component Builder UI");
                Desktop.getDesktop().browse(new URI("http://localhost:" + port));
            }
        } catch (ServletException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

}
