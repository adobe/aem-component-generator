package com.adobe.aem.compgenerator.utils;

import com.adobe.aem.compgenerator.exceptions.GeneratorException;
import com.adobe.aem.compgenerator.models.BaseModel;
import com.adobe.aem.compgenerator.models.GenerationConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateUtils {
    private static final String TEMPLATE_DEFINITIONS = "$['options']['template-definitions']";
    private static final String TEMPLATE_COPY_PATTERN_BEFORE = TEMPLATE_DEFINITIONS + "['copy-patterns']";
    private static final String TEMPLATE_FIELDS_WITH_PLACEHOLDERS =
            TEMPLATE_DEFINITIONS + "['placeholder-patterns'].jsonPath";
    private static final String TEMPLATE_COLLECT_PATTERN_AFTER = TEMPLATE_DEFINITIONS + "['collect-patterns']";
    private static final Logger LOG = LogManager.getLogger(TemplateUtils.class);

    static String initConfigTemplates(GenerationConfig generationConfig, String dataConfigJson) {
        String dataConfigLoc = dataConfigJson;
        try {
            // Copy template pattern to json nodes e.g. json-data properties
            dataConfigLoc = bringTemplateValuesInDataConfig(dataConfigJson, TEMPLATE_COPY_PATTERN_BEFORE);

            // Resolve JsonPath-Placeholders from copied template pattern
            dataConfigLoc = resolveRelativeJsonPathsInDataConfig(dataConfigLoc);

            // Build a template replacer Map from JsonPath-Placeholders and set it to generationConfig
            dataConfigLoc = resolveCollectPatternAfter(dataConfigLoc, TEMPLATE_COLLECT_PATTERN_AFTER,
                    "$.['options'].['replaceValueMap']", generationConfig);

            List<PathValueHolder<Object>> pathValueHolders =
                    readValuesFromJsonPath(dataConfigLoc, "$.['options'].['replaceValueMap']", null);
            Map valueMap = (Map) pathValueHolders.get(0).getValue();
            LOG.trace("Found valueMap: " + valueMap.toString());
            generationConfig.getOptions().setReplaceValueMap(valueMap);
            LOG.trace("Data-config templating used: \n{}", dataConfigLoc);
        } catch (Exception e) {
            throw new GeneratorException("initConfigTemplates Error while init config templates" + dataConfigLoc, e);
        }
        return dataConfigLoc;
    }

    static String getIntendedStringFromJson(Object dataConfig) {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            StringWriter outputWriter = new StringWriter();
            DocumentContext parse;
            if (dataConfig instanceof String) {
                //only String where indented correctly
                parse = JsonPath.parse((String) dataConfig);
            } else {
                parse = JsonPath.parse(dataConfig);
            }
            mapper.writeValue(outputWriter, (parse.json()));
            return outputWriter.toString();
        } catch (IOException e) {
            throw new GeneratorException("Error getIntendedStringFromJson for " + dataConfig, e);
        }
    }

    @SuppressWarnings("unchecked")
    static String resolveCollectPatternAfter(String dataConfig, String templateCollectPatternAfter,
            String targetPathjforReplacerValueMap, GenerationConfig generationConfig) {
        dataConfig = bringTemplateValuesInDataConfig(dataConfig, templateCollectPatternAfter);
        final List<PathValueHolder<Map>> pathValueHolders =
                readValuesFromJsonPath(dataConfig, targetPathjforReplacerValueMap, null);
        PathValueHolder<Map> replaceValueMap = pathValueHolders.get(0);
        for (Map.Entry<String, Object> replacerEntry : ((Map<String, Object>) replaceValueMap.getValue()).entrySet()) {
            String replacerKey = replacerEntry.getKey();
            String replacerJsonPathValue = (String) replacerEntry.getValue();
            LOG.trace("replaceValueMapPath {} replacerKey {} replacerJsonPathValue {}", replaceValueMap.getPath(),
                    replacerKey, replacerJsonPathValue);
            List<String> valuesfromReplacerJsonPathValue = new ArrayList<>();
            for (PathValueHolder<Object> replacerValue : readValuesFromJsonPath(dataConfig, replacerJsonPathValue,
                    null)) {
                LOG.trace("Found Values: " + replacerValue.getValue().toString());
                valuesfromReplacerJsonPathValue.add((String) replacerValue.getValue());
            }
            String templatePlaceholders = StringUtils.join(valuesfromReplacerJsonPathValue, "\n");
            Map<String, String> stringsToReplaceValueMap = CommonUtils.getStringsToReplaceValueMap(generationConfig);
            LOG.trace("Replace common placeholders within template placeholders:\n{} \nMap:\n{}" + templatePlaceholders,
                    stringsToReplaceValueMap.toString());
            StringSubstitutor stringSubstitutor = new StringSubstitutor(stringsToReplaceValueMap);
            templatePlaceholders = stringSubstitutor.replace(templatePlaceholders);
            LOG.trace("Replaced \n{}" + templatePlaceholders);
            dataConfig = setDataToJsonByJsonPath(dataConfig, replaceValueMap.getPath(), "@" + replacerKey,
                    templatePlaceholders);
        }
        return dataConfig;
    }

    /**
     * Templates in data config containing relative placeholder "@{...}")" e.g. for special property item like field label.
     * Example: <p>@{label}: ${${sightly}Model.@{field}}</p> becomes <p>@{label}: ${${sightly}Model.textfieldTest}</p>
     *
     * @param dataConfig ..
     * @return changed dataConfig
     * @throws IOException ..
     */
    private static String resolveRelativeJsonPathsInDataConfig(String dataConfig) throws IOException {
        Map<String, String> stringsToReplaceValueMap = new LinkedHashMap<>();
        String templateFinder =
                (String) readValuesFromJsonPath(dataConfig, TEMPLATE_FIELDS_WITH_PLACEHOLDERS, null).get(0).getValue();
        for (PathValueHolder<Object> objectPathValueHolder : readValuesFromJsonPath(dataConfig, templateFinder, null)) {
            String templateJasonPath = unifyJasonPath(objectPathValueHolder.getPath());
            try {
                StringWriter outputWriter = new StringWriter();
                new ObjectMapper().writeValue(outputWriter, objectPathValueHolder.getValue());
                String templateJsonValue = outputWriter.toString();
                LOG.trace("templateJsonValue {}", templateJsonValue);
                for (String templateToken : TemplateUtils.findTemplateTokens(templateJsonValue)) {
                    LOG.trace("templateToken {} for path {}", templateToken, templateJasonPath);
                    String templateParentPath = StringUtils.substringBeforeLast(templateJasonPath, ".");
                    String relativeJsonPath =
                            StringUtils.replace(StringUtils.substringBeforeLast(templateToken, "}"), "@{", "@");
                    String valueAsJsonFrom = (String) readValuesFromJsonPath(dataConfig,
                            buildJasonPath(templateParentPath, relativeJsonPath), null).get(0).getValue();
                    LOG.trace("put templateToken {} with value  {}", templateToken, valueAsJsonFrom);
                    stringsToReplaceValueMap
                            .put(StringUtils.substringBetween(templateToken, "{", "}"), valueAsJsonFrom);
                }
                StringSubstitutor stringSubstitutor = new StringSubstitutor(stringsToReplaceValueMap, "@{", "}");
                dataConfig = setDataToJsonByJsonPath(dataConfig, templateJasonPath, "@",
                        new ObjectMapper().readValue(stringSubstitutor.replace(templateJsonValue), Object.class));
            } catch (JsonProcessingException e) {
                LOG.warn("Problem reading template Json for path " + templateJasonPath, e);
            }
        }
        return dataConfig;
    }

    static List<String> findTemplateTokens(final CharSequence text) {
        if (text == null || text.toString().trim().equals("")) {
            throw new IllegalArgumentException("Invalid text");
        }
        final Pattern pattern = Pattern.compile("(@\\{[^\\}]*\\})+");
        final Matcher matcher = pattern.matcher(text.toString());
        final List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group(0));
        }
        return tokens;
    }

    /**
     * @param dataConfigJson         ..
     * @param definitionTypeNodeName node name to search for template definition
     * @return dataConfigJson replaced by values
     */
    private static List<TemplateDefinition> readTemplateDefinition(String dataConfigJson,
            String definitionTypeNodeName) {
        List<TemplateDefinition> templateDefinitions = new ArrayList<>();
        List<PathValueHolder<Map<String, String>>> foundDefinitionTypes =
                readValuesFromJsonPath(dataConfigJson, definitionTypeNodeName + ".*", null);
        for (PathValueHolder<Map<String, String>> foundDefinitionType : foundDefinitionTypes) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String definitionAsJsonString = getIntendedStringFromJson(foundDefinitionType.getValue());
            try {
                templateDefinitions.add(mapper.readValue(definitionAsJsonString, TemplateDefinition.class));
            } catch (JsonProcessingException e) {
                throw new GeneratorException(
                        "Error DeserializationFeature path " + foundDefinitionType.getPath() + " for " +
                                definitionAsJsonString, e);
            }
        }
        return templateDefinitions;
    }

    /**
     * bringTemplateValuesInDataConfig
     *
     * @param dataConfigJson         ..
     * @param definitionTypeNodeName node to search for TemplateValues
     * @return dataConfigJson replaced by values
     */
    private static String bringTemplateValuesInDataConfig(String dataConfigJson, String definitionTypeNodeName) {
        String dataConfigLocal = dataConfigJson;
        List<TemplateDefinition> collectPatterns = readTemplateDefinition(dataConfigLocal, definitionTypeNodeName);
        for (TemplateDefinition collectPattern : collectPatterns) {
            String baseJsonPath = collectPattern.getbaseJsonPath();
            if (collectPattern.getTargetAttributes() != null) {
                for (Map.Entry<String, String> patternAttributes : collectPattern.getTargetAttributes().entrySet()) {
                    String replacerKey = patternAttributes.getKey();
                    String replacerJsonPathValue = patternAttributes.getValue();
                    String jsonPathToSearchInDataJson = unifyJasonPath(baseJsonPath);
                    List<String> jsonPathsToAdd =
                            readPathsFromJsonPath(dataConfigLocal, jsonPathToSearchInDataJson, null);
                    LOG.trace("bringTemplateValuesInDataConfig - jsonPathToSearchInDataJson {} jsonPathToAdd {} found",
                            jsonPathToSearchInDataJson, jsonPathsToAdd);
                    for (String jsonPathToAdd : jsonPathsToAdd) {
                        dataConfigLocal = setDataToJsonByJsonPath(dataConfigLocal, jsonPathToAdd, replacerKey,
                                replacerJsonPathValue);
                    }

                }
            }
        }
        return dataConfigLocal;
    }

    /**
     * setDataToJsonByJsonPath missing nodes will be created with put LinkedHashMap
     *
     * @param dataConfigJson   ..
     * @param jsonPathToAdd    starts with $.
     * @param relativeJsonPath starts with @
     * @param targetValue      ..
     * @return jsonString with set value
     */
    static String setDataToJsonByJsonPath(String dataConfigJson, String jsonPathToAdd, String relativeJsonPath,
            Object targetValue) {
        String targetPath = buildJasonPath(jsonPathToAdd, relativeJsonPath);
        String parentAdded = StringUtils.substringBefore(targetPath, ".");
        DocumentContext jsonDoc = JsonPath.using(Configuration.builder().build()).parse(dataConfigJson);
        String[] pathSegmentsAfterRoot =
                StringUtils.split(StringUtils.substringAfter(targetPath, parentAdded + "."), ".");
        for (String currentPathSegment : pathSegmentsAfterRoot) {
            Object subscription = null;
            String currentTargetPath = parentAdded + "." + currentPathSegment;
            try {
                subscription = jsonDoc.read(currentTargetPath);
            } catch (Exception e) {
                LOG.trace("Node {} not found", currentTargetPath);
            }
            if (subscription == null ||
                    (subscription instanceof Collection && ((Collection) subscription).size() <= 0)) {
                try {
                    LOG.debug("Put Node parentAdded {} currentPathSegment {} ", parentAdded, currentPathSegment);
                    jsonDoc = jsonDoc.put(parentAdded, currentPathSegment, new LinkedHashMap());
                    LOG.trace("Change json parentAdded {} currentPathSegment {} ", parentAdded, currentPathSegment);
                } catch (Exception e) {
                    throw new GeneratorException(
                            "Error creating node " + currentPathSegment + " at " + parentAdded + " ", e);
                }
            }
            parentAdded = currentTargetPath;
        }
        LOG.trace("Set at targetPath {} targetValue {}", targetPath, targetValue);
        return jsonDoc.set(targetPath, targetValue).jsonString();
    }

    private static String buildJasonPath(String jsonPathToAdd, String relativeJsonPath) {
        String unifiedJsonPathToAdd = unifyJasonPath(jsonPathToAdd);
        if (!StringUtils.startsWithAny(unifiedJsonPathToAdd, "$")) {
            throw new GeneratorException("jsonPathToAdd [" + jsonPathToAdd + "] must start with $ or \"['$']\"");
        }
        if (!StringUtils.startsWith(relativeJsonPath, "@")) {
            throw new GeneratorException("relativeJsonPath [" + relativeJsonPath + "] must start with @");
        }
        String buildJsonPath = StringUtils.replace(relativeJsonPath, "@", unifiedJsonPathToAdd);
        LOG.trace("buildJasonPath - targetPath {} jsonPathToAdd {} relativeJsonPath {}", buildJsonPath,
                unifiedJsonPathToAdd, relativeJsonPath);
        return buildJsonPath;
    }

    static String unifyJasonPath(String jsonPathToAdd) {
        String unifiedJsonPathToAdd = jsonPathToAdd;
        if (StringUtils.startsWith(unifiedJsonPathToAdd, "['") && StringUtils.endsWith(unifiedJsonPathToAdd, "']")) {
            unifiedJsonPathToAdd =
                    StringUtils.substringBeforeLast(StringUtils.substringAfter(unifiedJsonPathToAdd, "['"), "']");
        }
        unifiedJsonPathToAdd = StringUtils.replace(unifiedJsonPathToAdd, ".['$", "['$");
        unifiedJsonPathToAdd = StringUtils.replace(unifiedJsonPathToAdd, "$[", "$.[");
        unifiedJsonPathToAdd = StringUtils.replace(unifiedJsonPathToAdd, "][", "].[");
        LOG.trace("unifyJasonPath - jsonPathToAdd {} unifiedJsonPathToAdd {}", jsonPathToAdd, unifiedJsonPathToAdd);
        return unifiedJsonPathToAdd;
    }

    static <T> List<PathValueHolder<T>> readValuesFromJsonPath(String dataConfigJson, String jasonPath,
            Predicate<String> filterPaths) {
        final List<String> pathsFound = readPathsFromJsonPath(dataConfigJson, jasonPath, filterPaths);
        final List<PathValueHolder<T>> pathValues = new ArrayList<PathValueHolder<T>>();
        for (String path : pathsFound) {
            String unifyJasonPath = unifyJasonPath(path);
            LOG.trace("readValuesFromJsonPath - unifyJasonPath {}", unifyJasonPath);
            try {
                pathValues.add(new PathValueHolder<>(unifyJasonPath,
                        JsonPath.parse(dataConfigJson).delete("$..['_comment_']").read(unifyJasonPath)));
            } catch (Exception e) {
                throw new GeneratorException("Read value error on reading jsonPath '" + unifyJasonPath +
                        ". See http://jsonpath.herokuapp.com for help", e);
            }
        }
        return pathValues;
    }

    static List<String> readPathsFromJsonPath(String dataConfigJson, String readPath, Predicate<String> filterPaths) {
        Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
        String unifyJasonPath = unifyJasonPath(readPath);
        LOG.trace("readPathsFromJsonPath - readPath {}", unifyJasonPath);
        List<String> pathList = new ArrayList<>();
        try {
            pathList = JsonPath.using(conf).parse(dataConfigJson).delete("$..['_comment_']").read(unifyJasonPath);
            if (filterPaths != null) {
                return Arrays.asList(pathList.stream().filter(filterPaths).toArray(String[]::new));
            }
        } catch (PathNotFoundException e) {
            LOG.trace("readPathsFromJsonPath - dataConfigJson {}", dataConfigJson);
            LOG.warn("readPathsFromJsonPath - readPath " + unifyJasonPath, e);
        } catch (Exception e) {
            String urlForTesting = null;
            try {
                urlForTesting = URLEncoder.encode("\"http://jsonpath.herokuapp.com/?path=" + unifyJasonPath, "UTF-8");
            } catch (UnsupportedEncodingException er) {
                LOG.error("Error encoding parameter {}", er.getMessage(), er);
            }
            throw new GeneratorException(
                    "Read path error on reading jsonPath '" + unifyJasonPath + "' \ntest your jsonPath with " +
                            urlForTesting + "\"\njson: " + dataConfigJson, e);
        }
        return pathList;
    }

    static class TemplateDefinition implements BaseModel {

        @JsonProperty("baseJsonPath")
        private String baseJsonPath;

        @JsonProperty("targetAttributes")
        private Map<String, String> targetAttributes;

        String getbaseJsonPath() {
            return baseJsonPath;
        }

        void setbaseJsonPath(String baseJsonPath) {
            this.baseJsonPath = baseJsonPath;
        }

        Map<String, String> getTargetAttributes() {
            return targetAttributes;
        }

        public void setTargetAttributes(Map<String, String> targetAttributes) {
            this.targetAttributes = targetAttributes;
        }

        @Override
        public boolean isValid() {
            return this.baseJsonPath != null;
        }
    }


    static class PathValueHolder<T> {
        private final String path;
        private final T value;

        PathValueHolder(String path, T value) {
            this.path = path;
            this.value = value;
        }

        public String getPath() {
            return path;
        }

        public T getValue() {
            return value;
        }
    }

}
