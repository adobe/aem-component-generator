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
            if (!configFile.exists()) {
                throw new GeneratorException("Error : No config file found.");
            }
            //Json file to ComponentData Object
            GenerationConfig generationConfig = ComponentGeneratorUtils.getComponentData(configFile);

            if (generationConfig != null) {
                ComponentGeneratorUtils.createComponentFileStructure(generationConfig);
                if (generationConfig.getOptions().isHasSlingModel()) {
                    JavaCodeModel javaCodeModel = new JavaCodeModel(generationConfig);
                    javaCodeModel._createSlingModel();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
