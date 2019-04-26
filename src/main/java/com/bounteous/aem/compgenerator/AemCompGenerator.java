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
import com.bounteous.aem.compgenerator.utils.ComponentGeneratorUtils;

import java.io.File;

public class AemCompGenerator {

    public static void main(String[] args) {
        try {
            String configPath = "data-config.json";
            if (args != null && args.length > 0) {
                configPath = args[0];
            }

            File configFile = new File(configPath);

            if (ComponentGeneratorUtils.isFileBlank(configFile)) {
                throw new GeneratorException("Config file missing / empty.");
            }

            //Json file to ComponentData Object
            GenerationConfig generationConfig = ComponentGeneratorUtils.getComponentData(configFile);

            if (generationConfig == null) {
                throw new GeneratorException("Config file is empty / null !!");
            }

            ComponentGeneratorUtils._buildComponent(generationConfig);

            if (generationConfig.getOptions().isHasSlingModel()) {
                JavaCodeModel javaCodeModel = new JavaCodeModel();
                javaCodeModel._buildSlingModel(generationConfig);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("---------------------------");
            e.printStackTrace();
        }
    }
}
