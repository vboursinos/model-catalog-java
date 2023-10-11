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
import java.util.stream.Collectors;

public class CreateInsertScripts {
    private static final Logger logger = LogManager.getLogger(FileUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String JSON_DIR_PATH = "model_infos";
    private static final String SQL_DIR_PATH = "sql_scripts";

    public void insertDataScripts() {
        ObjectMapper mapper = new ObjectMapper();
        Set<String> allModelTypes = new HashSet<>();
        Set<String> allModelStructureTypes = new HashSet<>();
        Set<String> allMlTasks = new HashSet<>();
        Set<String> allMetrics = new HashSet<>();
        processModelsInDirectory(mapper, Paths.get(JSON_DIR_PATH),
                models -> allModelTypes.addAll(getModelTypes(models)));
        processModelsInDirectory(mapper, Paths.get(JSON_DIR_PATH),
                models -> allModelStructureTypes.addAll(getModelStructureTypes(models)));
        processModelsInDirectory(mapper, Paths.get(JSON_DIR_PATH),
                models -> allMlTasks.addAll(getMlTasks(models)));
        processModelsInDirectory(mapper, Paths.get(JSON_DIR_PATH),
                models -> allMetrics.addAll(getMetrics(models)));
        FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, "insert_model_type.sql").toString(), buildInsertModelTypeSQL(allModelTypes));
        FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, "insert_group_type.sql").toString(), buildInsertGroupTypeSQL());
        FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, "insert_model_structure_type.sql").toString(), buildInsertModelStructureTypeSQL(allModelStructureTypes));
        FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, "insert_ml_task.sql").toString(), buildInsertMlTaskSQL(allMlTasks));
        FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, "insert_metrics.sql").toString(), buildMetricSQL(allMetrics));
        logger.info(allModelTypes.toString());
        processModelsInDirectory(mapper, Paths.get(JSON_DIR_PATH), this::createModelSqlFile);
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
            sb.append("INSERT INTO ModelType(name) VALUES ('").append(modelType).append("');\n");
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

        sb.append(buildInsertIntoModelSQL(name, mlTask, metadata, model.isBlackListed()));
        sb.append(buildInsertIntoModelDependencySQL(name, mlTask, metadata));
        sb.append(buildInsertIntoModelToGroupSQL(name, mlTask, model.getGroups()));
        sb.append(buildInsertIntoParameterAndParameterValueSQL(name, mlTask, model, metadata));
        sb.append(buildInsertIntoModelMetadataSQL(name, mlTask, model, ensembleFamilies, metadata));
        sb.append(buildInsertIntoIncompatibleMetricSQL(name, model));
    }

    private static String buildInsertIntoIncompatibleMetricSQL(String name, Model model) {
        StringBuilder sb = new StringBuilder();
        for (String metric : model.getIncompatibleMetrics()) {
            sb.append("INSERT INTO IncompatibleMetric(modelId, metricName) VALUES ((select id from Model where name='").append(name).append("'), '").append(metric).append("');\n");
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

    private static String buildInsertIntoModelSQL(String name, String mlTask, Metadata metadata, boolean isBlackListed) {
        StringBuilder sb = new StringBuilder();

        String advantagesArray = "{" + String.join(",", metadata.getAdvantages()) + "}";
        String disadvantagesArray = "{" + String.join(",", metadata.getDisadvantages()) + "}";

        sb.append("INSERT INTO Model(name, mlTask, description, displayName, structure, advantages, disadvantages, isEnabled, modelTypeId) VALUES ('").
                append(name).append("', '").
                append(mlTask).append("', '").
                append(metadata.getModelDescription()).append("', '").
                append(metadata.getDisplayName()).append("', '").
                append(metadata.getStructure()).append("', '").
                append(advantagesArray).append("', '").
                append(disadvantagesArray).append("',").
                append(isBlackListed).append(",").
                append("(select id from ModelType where name='").
                append(metadata.getModelType().get(0)).append("'));\n");

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
            sb.append("INSERT INTO ModelToGroup(modelId, groupId) VALUES ((select id from Model where name='").
                    append(name).append("' and mlTask='").
                    append(mlTask).append("'),").
                    append("(select id from ModelGroup where name='").
                    append(group).append("'));\n");
        }

        return sb.toString();
    }

    private static String buildInsertIntoParameterAndParameterValueSQL(String name, String mlTask, Model model, Metadata metadata) {
        StringBuilder sb = new StringBuilder();

        List<String> prime = Optional.ofNullable(metadata.getPrime()).orElse(Collections.emptyList());

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

        sb.append(insertIntoParameterAndParameterValue(orderedParameters, name, mlTask));

        return sb.toString();
    }

    private static String insertIntoParameterAndParameterValue(List<HyperParameter> orderedParameters, String name, String mlTask) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (HyperParameter parameter : orderedParameters) {
            String description = parameter.getDescription().replace("'", "''");
            count++;
            sb.append(insertParameterSQL(parameter, description, count, name, mlTask));
//
//            for (String value : parameter.getValues()) {
//                value = value.replace("'", "''");
//                sb.append(insertParameterValueSQL(value, parameter, name, mlTask));
//            }
        }
        return sb.toString();
    }

    private static String insertParameterSQL(HyperParameter parameter, String description, int count, String name, String mlTask) {
        return new StringBuilder("INSERT INTO Parameter(parameterType, name, defaultValue, label, description, enabled, hasConstraint, constraintInformation, fixedValue, ordering, modelId) VALUES ('")
//                .append(parameter.getParameterType()).append("', '")
                .append(parameter.getName()).append("', '")
                .append(parameter.getDefaultValue()).append("',")
                .append("'").append(parameter.getLabel()).append("',")
                .append("'").append(description).append("',")
                .append(parameter.getEnabled()).append(",")
                .append(parameter.getConstraint()).append(",")
                .append("'").append(parameter.getConstraintInformation()).append("',")
//                .append(parameter.isFixedValue()).append(",")
                .append(count).append(",")
                .append("(select id from Model where name='").append(name).append("' and mlTask='").append(mlTask).append("'));\n")
                .toString();
    }

    private static String insertParameterValueSQL(String value, InputParameter parameter, String name, String mlTask) {
        return new StringBuilder("INSERT INTO ParameterValue(value,parameterId) VALUES ('")
                .append(value).append("',(select id from Parameter where name='").append(parameter.getParameterName()).append("' and modelId=(select id from model where name='").append(name).append("' and mlTask='").append(mlTask).append("')));\n")
                .toString();
    }

    private static String buildInsertIntoModelMetadataSQL(String name, String mlTask, Model model, List<EnsembleFamily> ensembleFamilies, Metadata metadata) {
        StringBuilder sb = new StringBuilder();
        EnsembleFamily matchingFamily = null;
        for (EnsembleFamily family : ensembleFamilies) {
            if (family.getName().equals(model.getName())) {
                matchingFamily = family;
                break;
            }
        }

        if (matchingFamily != null) {
            sb.append("INSERT INTO ModelMetadata(modelId, probabilities, featureImportances, decisionTree, ensembleType, family) VALUES ((select id from Model where name='")
                    .append(name).append("' and mlTask='")
                    .append(mlTask).append("'),")
                    .append(metadata.getSupports().getProbabilities()).append(",")
                    .append(metadata.getSupports().getFeatureImportance()).append(",")
                    .append(metadata.getSupports().getDecisionTree()).append(",'")
                    .append(matchingFamily.getEnsembleType()).append("','")
                    .append(matchingFamily.getFamily()).append("');\n");
        }
        return sb.toString();
    }

}
