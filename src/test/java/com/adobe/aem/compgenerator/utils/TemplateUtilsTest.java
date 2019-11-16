package com.adobe.aem.compgenerator.utils;

import com.adobe.aem.compgenerator.exceptions.GeneratorException;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


class TemplateUtilsTest {
    GenerationConfig generationConfig;
    String configFilePath;
    File configFile;
    String dataConfigJson;

    @BeforeEach
    void setUp() throws IOException {
        configFilePath = "/component-generator/data-config.json";
        configFile = new File(this.getClass().getResource(configFilePath).getFile());
        generationConfig = CommonUtils.getComponentData(configFile);
        generationConfig.setConfigFilePath(configFilePath);
        dataConfigJson = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);

    }

    @Test
    void testInitConfigTemplates() throws Exception {
        String dataConfigJsonNew = TemplateUtils.initConfigTemplates(generationConfig, dataConfigJson);
        Assertions.assertNotEquals(dataConfigJson, dataConfigJsonNew);

        TemplateUtils.TemplateDefinition templateDefinition = new TemplateUtils.TemplateDefinition();
        Assertions.assertFalse(templateDefinition.isValid());
        templateDefinition.setbaseJsonPath("asd");
        Assertions.assertTrue(templateDefinition.isValid());
    }

    @Test
    void testReadValuesFromJsonPath() {
        String testJson = "{ \"root\" : { \"node1\" : \"zui\" ,  \"node2\" : \"asd\" ,  \"node3\" : \"rtz}\" } }";
        List<TemplateUtils.PathValueHolder<Map>> copySourceBefore =
                TemplateUtils.readValuesFromJsonPath(testJson, "$.root[*]", null);
        Assertions.assertEquals("zui", copySourceBefore.get(0).getValue());
    }

    @Test
    void testReadPathListFromJsonPath() throws IOException {
        String testJson = "{ \"root\" : { \"node1\" : \"zui\" ,  \"node2\" : \"asd\" ,  \"node3\" : \"rtz}\" } }";

        List<String> pathListFiltered =
                TemplateUtils.readPathsFromJsonPath(testJson, "$.root.*", path -> StringUtils.endsWith(path, "e3']"));
        Assertions.assertArrayEquals(new String[]{"$['root']['node3']"}, pathListFiltered.toArray(new String[]{}));

        pathListFiltered = TemplateUtils.readPathsFromJsonPath(testJson, "$.rootNotThere.*", null);
        Assertions.assertArrayEquals(new String[]{}, pathListFiltered.toArray(new String[]{}));

        Assertions.assertThrows(GeneratorException.class, () -> {
            TemplateUtils.readPathsFromJsonPath(testJson, "$rootInvalid.*", null);
        });
    }

    @Test
    void testSetDataToJsonByJsonPath() throws JsonProcessingException {
        String testJson = "{ \"root\" : { \"node1\" : { \"xml\" : \"asd\" } } }";
        String targetValue = "\"./template-htl.webcomponent.html.txt\"";

        String jsonPathToAdd = "$.['root','root'].node1";
        String relativeJsonPath = "@.html.other";
        String newJson = TemplateUtils.setDataToJsonByJsonPath(testJson, jsonPathToAdd, relativeJsonPath, targetValue);
        Assertions.assertEquals(targetValue, TemplateUtils.readValuesFromJsonPath(newJson,
                StringUtils.replace(relativeJsonPath, "@", TemplateUtils.unifyJasonPath(jsonPathToAdd)), null).get(0)
                .getValue());

        jsonPathToAdd = "['$.root']";
        relativeJsonPath = "@.node2.html";
        newJson = TemplateUtils.setDataToJsonByJsonPath(testJson, jsonPathToAdd, relativeJsonPath, targetValue);
        Assertions.assertEquals(targetValue, TemplateUtils.readValuesFromJsonPath(newJson,
                StringUtils.replace(relativeJsonPath, "@", TemplateUtils.unifyJasonPath(jsonPathToAdd)), null).get(0)
                .getValue());

        jsonPathToAdd = "['$']";
        relativeJsonPath = "@.root.html";
        newJson = TemplateUtils.setDataToJsonByJsonPath(testJson, jsonPathToAdd, relativeJsonPath, targetValue);
        Assertions.assertEquals(targetValue, TemplateUtils.readValuesFromJsonPath(newJson,
                StringUtils.replace(relativeJsonPath, "@", TemplateUtils.unifyJasonPath(jsonPathToAdd)), null).get(0)
                .getValue());
    }

    @Test
    void testSetDataToJsonByJsonPathObject() throws JsonProcessingException {
        String jsonPathToAdd = "$.root";
        String relativeJsonPath = "@";
        String testJson = "{ \"html\" : { \"node1\" : \"asd\" } }";

        String newJson = TemplateUtils.setDataToJsonByJsonPath(testJson, jsonPathToAdd, relativeJsonPath,
                new ObjectMapper().readValue(testJson, Object.class));
        Assertions.assertEquals("asd",
                TemplateUtils.readValuesFromJsonPath(newJson, "$.root.html.node1", null).get(0).getValue());
    }

    @Test
    void testFindTemplateTokens() {
        String testJson =
                "{ \"root\" : { \"node1\" : \"a${sd@{myToken1}as}d\" }, { \"node2\" : \"@{myToken2}\" }, { \"node3\" : \"@{myToken3}asd}\" }}";
        Assertions.assertEquals(Arrays.asList(new String[]{"@{myToken1}", "@{myToken2}", "@{myToken3}"}),
                TemplateUtils.findTemplateTokens(testJson));
    }

    @Test
    void testResolveCollectPatternAfter() {
        String content1 = "a${sightly}d";
        String content1Expected = "ademoCompd";
        String content2 = "asd2";
        //@formatter:off
        String nodes =
            "\"nodes\" : { " +
                "\"node1\" : { \"content\" : \"" + content1 + "\" }," +
                "\"node2\" : { \"content\" : \"" + content2 + "\" }" +
            "}";
        String patterns =
            "\"myPattern\" : [{"+
                "\"baseJsonPath\" : \"$\"," +
                "\"targetAttributes\": { \"@.options.replaceValueMap.content\" : \"$.nodes..[?(@.content)].content\" }" +
            "}]";
        String options =
            "\"options\" : {}";
        String optionsExpected = "\"options\" : { \"replaceValueMap\" : { \"content\" : \"" + content1Expected + "\n" + content2 + "\" } } }" ;
        //@formatter:on
        String testJson = "{" + nodes + "," + patterns + "," + options + "}";
        String expectJson = "{" + nodes + "," + patterns + "," + optionsExpected + "}";

        String templateCollectPatternAfter = "$['myPattern']";
        String targetPathForReplacerValueMap = "$.['options'].['replaceValueMap']";
        String dataConfigJson = TemplateUtils
                .resolveCollectPatternAfter(testJson, templateCollectPatternAfter, targetPathForReplacerValueMap,
                        generationConfig);

        Assertions.assertEquals(TemplateUtils.getIntendedStringFromJson(expectJson),
                TemplateUtils.getIntendedStringFromJson(dataConfigJson));
    }
}
