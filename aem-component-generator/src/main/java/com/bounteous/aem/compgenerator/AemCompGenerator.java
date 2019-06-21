/*
 * #%L
 * AEM Component Generator
 * %%
 * Copyright (C) 2019 Bounteous
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
package com.bounteous.aem.compgenerator;

import com.bounteous.aem.compgenerator.exceptions.GeneratorException;
import com.bounteous.aem.compgenerator.javacodemodel.JavaCodeModel;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.bounteous.aem.compgenerator.utils.CommonUtils;
import com.bounteous.aem.compgenerator.utils.ComponentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

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
        try {
            String configPath = "data-config.json";
            if (args.length > 0) {
                configPath = args[0];
            }

            File configFile = new File(configPath);

            if (CommonUtils.isFileBlank(configFile)) {
                throw new GeneratorException("Config file missing / empty.");
            }

            GenerationConfig config = CommonUtils.getComponentData(configFile);

            if (config == null) {
                throw new GeneratorException("Config file is empty / null !!");
            }

            if (!config.isValid() || !CommonUtils.isModelValid(config.getProjectSettings())) {
                throw new GeneratorException("Mandatory fields missing in the data-config.json !");
            }

            String compDir = config.getProjectSettings().getAppsPath() + "/"
                    + config.getProjectSettings().getComponentPath() + "/"
                    + config.getType() + "/" + config.getName();
            config.setCompDir(compDir);

            //builds component folder and file structure.
            ComponentUtils generatorUtils = new ComponentUtils(config);
            generatorUtils.buildComponent();

            //builds sling model based on config.
            if (config.getOptions() != null && config.getOptions().isHasSlingModel()) {
                JavaCodeModel javaCodeModel = new JavaCodeModel();
                javaCodeModel.buildSlingModel(config);
            }
        } catch (Exception e) {
            LOG.error("Failed to generate aem component.", e);
        }
    }
}
