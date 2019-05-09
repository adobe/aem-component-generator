/*
 * ***********************************************************************
 * BOUNTEOUS CONFIDENTIAL
 * ___________________
 *
 * Copyright 2019 Bounteous
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of Bounteous and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Bounteous
 * and its suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Bounteous.
 * ***********************************************************************
 */
package com.bounteous.aem.compgenerator;

import com.bounteous.aem.compgenerator.exceptions.GeneratorException;
import com.bounteous.aem.compgenerator.javacodemodel.JavaCodeModel;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import com.bounteous.aem.compgenerator.utils.CommonUtils;
import com.bounteous.aem.compgenerator.utils.ComponentUtils;

import java.io.File;

/**
 * Root of the AEM Component generator.
 *
 * AemCompGenerator reads the json data file input and creates folder, file
 * structure of an AEM component and sling model interface with member values
 * and getters.
 */
public class AemCompGenerator {

    private static GenerationConfig config;


    public static void main(String[] args) {
        try {
            String configPath = "data-config.json";
            if (args != null && args.length > 0) {
                configPath = args[0];
            }

            File configFile = new File(configPath);

            if (CommonUtils.isFileBlank(configFile)) {
                throw new GeneratorException("Config file missing / empty.");
            }

            config = CommonUtils.getComponentData(configFile);

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
            generatorUtils._buildComponent();

            //builds sling model based on config.
            if (config.getOptions() != null && config.getOptions().isHasSlingModel()) {
                JavaCodeModel javaCodeModel = new JavaCodeModel();
                javaCodeModel._buildSlingModel(config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
