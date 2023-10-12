import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.PropertiesConfig;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CreateInsertScripts {
    private static final Logger logger = LogManager.getLogger(FileUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String JSON_DIR_PATH = "model_infos";
    private static final String SQL_DIR_PATH = "sql_scripts";

    public void insertDataScripts() {
        ObjectMapper mapper = new ObjectMapper();

        //Get unique values from JSON files
        Path dirPath = Paths.get(JSON_DIR_PATH);
        Set<String> allModelTypes = extractUniqueValues(mapper, dirPath, CreateInsertScripts::getModelTypes);
        Set<String> allModelStructureTypes = extractUniqueValues(mapper, dirPath, CreateInsertScripts::getModelStructureTypes);
        Set<String> allMlTasks = extractUniqueValues(mapper, dirPath, CreateInsertScripts::getMlTasks);
        Set<String> allMetrics = extractUniqueValues(mapper, dirPath, CreateInsertScripts::getMetrics);

        // Write values to SQL files
        createSQLFile("insert_model_type.sql", buildInsertModelTypeSQL(allModelTypes));
        createSQLFile("insert_group_type.sql", buildInsertGroupTypeSQL());
        createSQLFile("insert_parameter_type.sql", buildParameterTypeSQL());
        createSQLFile("insert_distribution.sql", buildDistributionSQL());
        createSQLFile("insert_model_structure_type.sql", buildInsertModelStructureTypeSQL(allModelStructureTypes));
        createSQLFile("insert_ml_task.sql", buildInsertMlTaskSQL(allMlTasks));
        createSQLFile("insert_metrics.sql", buildMetricSQL(allMetrics));

        logger.info(allModelTypes.toString());
        processModelsInDirectory(mapper, dirPath, this::createModelSqlFile);
    }

    private void createSQLFile(String fileName, String content) {
        FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, fileName).toString(), content);
    }

    private Set<String> extractUniqueValues(ObjectMapper mapper, Path dirPath, Function<Models, Set<String>> valueExtractor) {
        Set<String> uniqueValues = new HashSet<>();
        processModelsInDirectory(mapper, dirPath, models -> uniqueValues.addAll(valueExtractor.apply(models)));
        return uniqueValues;
    }

    private void processModelsInDirectory(ObjectMapper mapper, Path dirPath, Consumer<Models> modelProcessor) {
        try {
            Files.newDirectoryStream(dirPath).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        modelProcessor.accept(mapper.readValue(filePath.toFile(), Models.class));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (IOException e) {
            logger.error("Error reading files from directory: " + e.getMessage(), e);
        }
    }

    private void createModelSqlFile(Models models) {
        String mltask = models.getModels().get(0).getMlTask();
        String sqlScript = buildInsertSQL(models);
        FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, String.format("insert_models_%s.sql", mltask)).toString(), sqlScript);
        logger.info(mltask + " sql file created successfully!");
    }

    static Set<String> getModelTypes(Models models) {
        Set<String> modelTypes = new HashSet<>();
        for (Model model : models.getModels()) {
            List<String> modelType = model.getMetadata().getModelType();
            if (modelType != null && !modelType.isEmpty()) {
                modelTypes.add(modelType.get(0));
            }
        }
        return modelTypes;
    }

    static Set<String> getModelStructureTypes(Models models) {
        Set<String> modelStructureTypes = new HashSet<>();
        for (Model model : models.getModels()) {
            String modelStructure = model.getMetadata().getStructure();
            modelStructureTypes.add(modelStructure);
        }
        return modelStructureTypes;
    }

    static Set<String> getMlTasks(Models models) {
        Set<String> mlTaskSet = new HashSet<>();
        for (Model model : models.getModels()) {
            String mlTask = model.getMlTask();
            mlTaskSet.add(mlTask);
        }
        return mlTaskSet;
    }

    static Set<String> getMetrics(Models models) {
        Set<String> metrics = new HashSet<>();
        for (Model model : models.getModels()) {
            List<String> incompatibleMetrics = model.getIncompatibleMetrics();
            if (incompatibleMetrics != null && !incompatibleMetrics.isEmpty()) {
                metrics.addAll(incompatibleMetrics);
            }
        }
        return metrics;
    }

    static String buildInsertModelTypeSQL(Set<String> modelTypes) {
        StringBuilder sb = new StringBuilder();
        for (String modelType : modelTypes) {
            sb.append("INSERT INTO model_type(name) VALUES ('").append(modelType).append("');\n");
        }
        return sb.toString();
    }

    static String buildInsertModelStructureTypeSQL(Set<String> modelStructureTypes) {
        StringBuilder sb = new StringBuilder();
        for (String modelStructureType : modelStructureTypes) {
            sb.append("INSERT INTO model_structure_type(name) VALUES ('").append(modelStructureType).append("');\n");
        }
        return sb.toString();
    }

    static String buildInsertMlTaskSQL(Set<String> mlTaskSet) {
        StringBuilder sb = new StringBuilder();
        for (String mlTask : mlTaskSet) {
            sb.append("INSERT INTO ml_task(name) VALUES ('").append(mlTask).append("');\n");
        }
        return sb.toString();
    }

    static String buildMetricSQL(Set<String> metricSet) {
        StringBuilder sb = new StringBuilder();
        for (String metric : metricSet) {
            sb.append("INSERT INTO metric(name) VALUES ('").append(metric).append("');\n");
        }
        return sb.toString();
    }

    static String buildInsertGroupTypeSQL() {
        StringBuilder sb = new StringBuilder();
        Properties properties = PropertiesConfig.getProperties();
        String groupsProperty = properties.getProperty("groups");
        List<String> groups = Arrays.asList(groupsProperty.split(","));

        for (String group : groups) {
            sb.append("INSERT INTO group_type(name) VALUES ('").append(group).append("');\n");
        }
        return sb.toString();
    }

    static String buildParameterTypeSQL() {
        StringBuilder sb = new StringBuilder();
        Properties properties = PropertiesConfig.getProperties();
        String parameterTypesProperty = properties.getProperty("parameter_types");
        List<String> parameterTypes = Arrays.asList(parameterTypesProperty.split(","));

        for (String parameterType : parameterTypes) {
            sb.append("INSERT INTO parameter_type(name) VALUES ('").append(parameterType).append("');\n");
        }
        return sb.toString();
    }

    static String buildDistributionSQL() {
        StringBuilder sb = new StringBuilder();
        Properties properties = PropertiesConfig.getProperties();
        String distributionProperty = properties.getProperty("distribution");
        List<String> distributionTypes = Arrays.asList(distributionProperty.split(","));

        for (String distributionType : distributionTypes) {
            sb.append("INSERT INTO parameter_distribution_type(name) VALUES ('").append(distributionType).append("');\n");
        }
        return sb.toString();
    }

    static String buildInsertSQL(Models models) {
        List<EnsembleFamily> ensembleFamilies = getEnsembleFamilies();

        StringBuilder sb = new StringBuilder();
        for (Model model : models.getModels()) {
            processModel(model, ensembleFamilies, sb);
        }

        return sb.toString();
    }

    private static List<EnsembleFamily> getEnsembleFamilies() {
        List<EnsembleFamily> ensembleFamilies = null;
        try {
            ensembleFamilies = objectMapper.readValue(Paths.get("static", "ensemble-family.json").toFile(), new TypeReference<List<EnsembleFamily>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ensembleFamilies;
    }

    private static void processModel(Model model, List<EnsembleFamily> ensembleFamilies, StringBuilder sb) {
        String name = model.getName().replace("'", "''");
        String mlTask = model.getMlTask().replace("'", "''");
        Metadata metadata = model.getMetadata();
        replaceSingleQuotesInMetadata(metadata);

        sb.append(buildInsertIntoModelSQL(model));
//        sb.append(buildInsertIntoModelDependencySQL(name, mlTask, metadata));
        sb.append(buildInsertIntoModelToGroupSQL(name, mlTask, model.getGroups()));
        sb.append(buildInsertIntoParameterAndParameterValueSQL(model));
        sb.append(buildInsertIntoModelMetadataSQL(model, ensembleFamilies));
        sb.append(buildInsertIntoIncompatibleMetricSQL(model));
        sb.append(buildInsertIntoParameterTypeDefinitionSQL(model));
        sb.append(buildInsertRestParameterTablesSQL(model));
    }

    private static List<ParameterTypeDistribution> getParameterTypeDistributionList(HyperParameter parameter) {
        List<ParameterTypeDistribution> parameterTypes = new ArrayList<>();
        Domain domain = parameter.getDomain();

        if (domain.getCategoricalSet() != null && !domain.getCategoricalSet().getCategories().isEmpty()) {
            String distributionType = (parameter.getDefaultValue().equals(true) || parameter.getDefaultValue().equals(false)) ? "boolean" : "categorical";
            parameterTypes.add(new ParameterTypeDistribution(distributionType, "uniform"));
        }

        if (domain.getFloatSet() != null && !domain.getFloatSet().getIntervals().isEmpty()) {
            parameterTypes.add(new ParameterTypeDistribution("float", parameter.getDistribution().getFloatDistribution()));
        }

        if (domain.getIntegerSet() != null && !domain.getIntegerSet().getRanges().isEmpty()) {
            parameterTypes.add(new ParameterTypeDistribution("integer", parameter.getDistribution().getIntegerDistribution()));
        }
        return parameterTypes;
    }

    private static String buildInsertIntoParameterTypeDefinitionSQL(Model model) {
        StringBuilder sb = new StringBuilder();

        for (HyperParameter parameter : model.getHyperParameters()) {
            List<ParameterTypeDistribution> parameterTypes = getParameterTypeDistributionList(parameter);
            int count = 0;

            for (ParameterTypeDistribution parameterType : parameterTypes) {
                sb.append("INSERT INTO parameter_type_definition(parameter_id, parameter_type_id, parameter_distribution_type_id, ordering) VALUES ((select id from parameter where name='")
                        .append(parameter.getName()).append("'  and model_id=(select id from model where name='").append(model.getName()).append("')), (select id from parameter_type where name='").append(parameterType.getParameterType()).append("'), ")
                        .append("(select id from parameter_distribution_type where name='").append(parameterType.getParameterDistribution()).append("'), ").append(count).append(");\n");
                count++;


            }
        }
        return sb.toString();
    }

    private static String buildInsertRestParameterTablesSQL(Model model) {
        StringBuilder sb = new StringBuilder();
        for (HyperParameter parameter : model.getHyperParameters()) {
            List<ParameterTypeDistribution> parameterTypes = getParameterTypeDistributionList(parameter);

            Object defaultValue = parameter.getDefaultValue();
            for (ParameterTypeDistribution parameterType : parameterTypes) {

                if (parameterType.getParameterType().equals("categorical")) {
                    String strDefaultValue = null;
                    if(defaultValue instanceof String) {
                        strDefaultValue = (String) defaultValue;
                    }
                    sb.append("INSERT INTO categorical_parameter(parameter_type_definition_id, default_value) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='").append(parameter.getName()).append("'  and model_id=(select id from model where name='").append(model.getName()).append("') and parameter_type_id=(select id from parameter_type where name='").append(parameterType.getParameterType()).append("'))), '").append(strDefaultValue).append("');\n");
                    for (Object value : parameter.getDomain().getCategoricalSet().getCategories()) {
                        sb.append("INSERT INTO categorical_parameter_value(parameter_type_definition_id, value ) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='").append(parameter.getName()).append("'  and model_id=(select id from model where name='").append(model.getName()).append("') and parameter_type_id=(select id from parameter_type where name='").append(parameterType.getParameterType()).append("'))), '").append(value).append("');\n");
                    }
                }
                if (parameterType.getParameterType().equals("float")){
                    Float floatDefaultValue = null;
                    if(defaultValue instanceof Float ) {
                        floatDefaultValue = (Float) defaultValue;
                    } else if(defaultValue instanceof Double) {
                        floatDefaultValue = ((Double) defaultValue).floatValue();
                    }
                    sb.append("INSERT INTO float_parameter(parameter_type_definition_id, default_value) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='").append(parameter.getName()).append("'  and model_id=(select id from model where name='").append(model.getName()).append("') and parameter_type_id=(select id from parameter_type where name='").append(parameterType.getParameterType()).append("'))), ").append(floatDefaultValue).append(");\n");
                    for (Interval interval : parameter.getDomain().getFloatSet().getIntervals()) {
                        sb.append("INSERT INTO float_parameter_range(parameter_type_definition_id, is_left_open, is_right_open, lower, upper ) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='").append(parameter.getName()).append("'  and model_id=(select id from model where name='").append(model.getName()).append("') and parameter_type_id=(select id from parameter_type where name='").append(parameterType.getParameterType()).append("'))), ").append(interval.getLeft()).append(", ").append(interval.getRight()).append(", ").append(interval.getLower()).append(", ").append(interval.getUpper()).append(");\n");
                    }
                }
                if (parameterType.getParameterType().equals("integer")){
                    Integer intDefaultValue = null;
                    if(defaultValue instanceof Integer) {
                        intDefaultValue = (Integer) defaultValue;
                    }
                    sb.append("INSERT INTO integer_parameter(parameter_type_definition_id, default_value) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='").append(parameter.getName()).append("'  and model_id=(select id from model where name='").append(model.getName()).append("') and parameter_type_id=(select id from parameter_type where name='").append(parameterType.getParameterType()).append("'))), ").append(intDefaultValue).append(");\n");
                    for (Range range : parameter.getDomain().getIntegerSet().getRanges()) {
                        sb.append("INSERT INTO integer_parameter_range(parameter_type_definition_id, start, stop ) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='").append(parameter.getName()).append("'  and model_id=(select id from model where name='").append(model.getName()).append("') and parameter_type_id=(select id from parameter_type where name='").append(parameterType.getParameterType()).append("'))), ").append(range.getStart()).append(", ").append(range.getStop()).append(");\n");
                    }
                }
                if (parameterType.getParameterType().equals("boolean")){
                    Boolean boolDefaultValue = null;
                    if(defaultValue instanceof Boolean) {
                        boolDefaultValue = (Boolean) defaultValue;
                    }
                    sb.append("INSERT INTO boolean_parameter(parameter_type_definition_id, default_value) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='").append(parameter.getName()).append("'  and model_id=(select id from model where name='").append(model.getName()).append("') and parameter_type_id=(select id from parameter_type where name='").append(parameterType.getParameterType()).append("'))), ").append(boolDefaultValue).append(");\n");
                }
            }
        }
        return sb.toString();
    }


    private static String buildInsertIntoIncompatibleMetricSQL(Model model) {
        StringBuilder sb = new StringBuilder();
        for (String metric : model.getIncompatibleMetrics()) {
            sb.append("INSERT INTO incompatible_metric(model_id, metric_id) VALUES ((select id from model where name='")
                    .append(model.getName()).append("'), (select id from metric where name='").append(metric).append("'));\n");
        }
        return sb.toString();
    }

    private static void replaceSingleQuotesInMetadata(Metadata metadata) {
        List<String> advantages = metadata.getAdvantages().stream()
                .map(s -> s.replace("'", "''"))
                .collect(Collectors.toList());
        metadata.setAdvantages(advantages);

        List<String> disadvantages = metadata.getDisadvantages().stream()
                .map(s -> s.replace("'", "''"))
                .collect(Collectors.toList());
        metadata.setDisadvantages(disadvantages);

        String modelDescription = metadata.getModelDescription().replace("'", "''");
        metadata.setModelDescription(modelDescription);
    }

    private static String buildInsertIntoModelSQL(Model model) {
        StringBuilder sb = new StringBuilder();

        String advantagesArray = "{" + String.join(",", model.getMetadata().getAdvantages()) + "}";
        String disadvantagesArray = "{" + String.join(",", model.getMetadata().getDisadvantages()) + "}";

        sb.append("INSERT INTO model(name, ml_task_id, description, display_name, structure_id, advantages, disadvantages, enabled, model_type_id) VALUES ('").
                append(model.getName()).append("', ").
                append("(select id from ml_task where name='").
                append(model.getMlTask()).append("'),'").
                append(model.getMetadata().getModelDescription()).append("', '").
                append(model.getMetadata().getDisplayName()).append("', ").
                append("(select id from model_structure_type where name='").
                append(model.getMetadata().getStructure()).append("'), '").
                append(advantagesArray).append("', '").
                append(disadvantagesArray).append("',").
                append(model.isBlackListed()).append(",").
                append("(select id from model_type where name='").
                append(model.getMetadata().getModelType().get(0)).append("'));\n");

        return sb.toString();
    }

    private static String buildInsertIntoModelDependencySQL(String name, String mlTask, Metadata metadata) {
        StringBuilder sb = new StringBuilder();

//        for (String dep : metadata.getDeps()) {
//            sb.append("INSERT INTO ModelDependency(modelId, name) VALUES ((select id from Model where name='").
//                    append(name).append("' and mlTask='").
//                    append(mlTask).append("'),").
//                    append("'").append(dep).
//                    append("');\n");
//        }
        return sb.toString();
    }

    private static String buildInsertIntoModelToGroupSQL(String name, String mlTask, List<String> modelGroups) {
        StringBuilder sb = new StringBuilder();

        for (String group : modelGroups) {
            sb.append("INSERT INTO model_group(model_id, group_id) VALUES ((select id from Model where name='").
                    append(name).append("'),").
                    append("(select id from group_type where name='").
                    append(group).append("'));\n");
        }

        return sb.toString();
    }

    private static String buildInsertIntoParameterAndParameterValueSQL(Model model) {
        StringBuilder sb = new StringBuilder();

        List<String> prime = Optional.ofNullable(model.getMetadata().getPrime()).orElse(Collections.emptyList());

        List<HyperParameter> parametersInPrime = new ArrayList<>();
        List<HyperParameter> parametersNotInPrime = new ArrayList<>();

        for (HyperParameter parameter : model.getHyperParameters()) {
            if (prime.contains(parameter.getName())) {
                parametersInPrime.add(parameter);
            } else {
                parametersNotInPrime.add(parameter);
            }
        }

        List<HyperParameter> orderedParameters = new ArrayList<>();
        orderedParameters.addAll(parametersInPrime);
        orderedParameters.addAll(parametersNotInPrime);

        sb.append(insertIntoParameterAndParameterValue(orderedParameters, model.getName()));

        return sb.toString();
    }

    private static String insertIntoParameterAndParameterValue(List<HyperParameter> orderedParameters, String name) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (HyperParameter parameter : orderedParameters) {
            String description = parameter.getDescription().replace("'", "''");
            count++;
            sb.append(insertParameterSQL(parameter, description, count, name));
//
//            for (String value : parameter.getValues()) {
//                value = value.replace("'", "''");
//                sb.append(insertParameterValueSQL(value, parameter, name, mlTask));
//            }
        }
        return sb.toString();
    }

    private static String insertParameterSQL(HyperParameter parameter, String description, int count, String name) {
        return new StringBuilder("INSERT INTO parameter(name, label, description, enabled, fixed_value, ordering, model_id) VALUES ('")
                .append(parameter.getName()).append("', '")
                .append(parameter.getLabel()).append("',")
                .append("'").append(description).append("',")
                .append(parameter.getEnabled()).append(",")
                .append(parameter.isFixedValue()).append(",")
                .append(count).append(",")
                .append("(select id from Model where name='").append(name)
                .append("'));\n")
                .toString();
    }

    private static String insertParameterValueSQL(String value, InputParameter parameter, String name, String mlTask) {
        return new StringBuilder("INSERT INTO ParameterValue(value,parameterId) VALUES ('")
                .append(value).append("',(select id from Parameter where name='").append(parameter.getParameterName()).append("' and modelId=(select id from model where name='").append(name).append("' and mlTask='").append(mlTask).append("')));\n")
                .toString();
    }

    private static String buildInsertIntoModelMetadataSQL(Model model, List<EnsembleFamily> ensembleFamilies) {
        StringBuilder sb = new StringBuilder();
        EnsembleFamily matchingFamily = null;
        for (EnsembleFamily family : ensembleFamilies) {
            if (family.getName().equals(model.getName())) {
                matchingFamily = family;
                break;
            }
        }

        if (matchingFamily != null) {
            sb.append("INSERT INTO model_metadata(model_id, decision_tree, ensemble_type, family) VALUES ((select id from model where name='")
                    .append(model.getName()).append("'),")
                    .append(model.getMetadata().getSupports().getDecisionTree()).append(",'")
                    .append(matchingFamily.getEnsembleType()).append("','")
                    .append(matchingFamily.getFamily()).append("');\n");
        }
        return sb.toString();
    }

}

