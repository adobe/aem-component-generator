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
                JavaCodeModel javaCodeModel = new JavaCodeModel(generationConfig);
                javaCodeModel._buildSlingModel();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("---------------------------");
            e.printStackTrace();
        }
    }
}
