import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.PropertiesConfig;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class CreateSqlScript {
    private static final Logger logger = LogManager.getLogger(CreateSqlScript.class);
    private static final String JSON_DIR_PATH = "model_infos";
    private static final String SQL_DIR_PATH = "sql_scripts";
    private static final String SETUP_SCRIPT_NAME = "setup.sql";

    public CreateSqlScript() {
        FileUtils.createDirectory(Paths.get(SQL_DIR_PATH));
    }

    public void createFiles() {
        createTablesScript();
        insertDataScripts();
    }

    public void createTablesScript() {
        String sqlScript = getSqlScript();
        writeToFile(Paths.get(SQL_DIR_PATH, SETUP_SCRIPT_NAME).toString(), sqlScript);
    }

    public void writeToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        } catch (IOException e) {
            logger.error("Error writing to file: " + e.getMessage(), e);
        }
    }

    //Methods for creating SQL scripts....
    private String getSqlScript() {
        return  "-- ModelType Table\n" +
                "CREATE TABLE ModelType (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    name VARCHAR(255)\n" +
                ");\n" +
                "\n" +
                "-- Model Table\n" +
                "CREATE TABLE Model (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    modelTypeId INTEGER,\n" +
                "    name TEXT,\n" +
                "    mlTask VARCHAR(255),\n" +
                "    description TEXT,\n" +
                "    displayName VARCHAR(255),\n" +
                "    structure VARCHAR(255),\n" +
                "    advantages TEXT[],\n" +
                "    disadvantages TEXT[],\n" +
                "    isEnabled BOOLEAN,\n" +
                "    FOREIGN KEY (modelTypeId) REFERENCES ModelType(id)\n" +
                ");\n" +
                "\n" +
                "-- ModelDependency Table\n" +
                "CREATE TABLE ModelDependency (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    modelId INTEGER,\n" +
                "    name VARCHAR(255),\n" +
                "    FOREIGN KEY (modelId) REFERENCES Model(id)\n" +
                ");\n" +
                "\n" +
                "-- ModelGroup Table\n" +
                "CREATE TABLE ModelGroup (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    name VARCHAR(255)\n" +
                ");\n" +
                "\n" +
                "-- ModelToGroup Table\n" +
                "CREATE TABLE ModelToGroup (\n" +
                "    modelId INTEGER,\n" +
                "    groupId INTEGER,\n" +
                "    FOREIGN KEY (modelId) REFERENCES Model(id),\n" +
                "    FOREIGN KEY (groupId) REFERENCES ModelGroup(id)\n" +
                ");\n" +
                "\n" +
                "-- ParameterType Table\n" +
                "CREATE TABLE ParameterType (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    name VARCHAR(255)\n" +
                ");\n" +
                "\n" +
                "-- Parameter Table\n" +
                "CREATE TABLE Parameter (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    modelId INTEGER,\n" +
                "    parameterType VARCHAR(255),\n" +
                "    name VARCHAR(255),\n" +
                "    minValue INTEGER,\n" +
                "    maxValue INTEGER,\n" +
                "    defaultValue VARCHAR(255),\n" +
                "    label VARCHAR(255),\n" +
                "    description TEXT,\n" +
                "    enabled BOOLEAN,\n" +
                "    hasConstraint BOOLEAN,\n" +
                "    constraintInformation TEXT,\n" +
                "    fixedValue BOOLEAN,\n" +
                "    ordering INTEGER,\n" +
                "    FOREIGN KEY (modelId) REFERENCES Model(id)\n" +
                ");\n" +
                "\n" +
                "-- ConstraintParameter Table\n" +
                "CREATE TABLE ConstraintParameter (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    modelId INTEGER,\n" +
                "    name VARCHAR(255),\n" +
                "    type VARCHAR(255),\n" +
                "    minValue DOUBLE PRECISION,\n" +
                "    maxValue DOUBLE PRECISION,\n" +
                "    FOREIGN KEY (modelId) REFERENCES Model(id)\n" +
                ");\n" +
                "\n" +
                "-- ConstraintParameterValue Table\n" +
                "CREATE TABLE ConstraintParameterValue (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    constraintParameterId INTEGER,\n" +
                "    value VARCHAR(255),\n" +
                "    FOREIGN KEY (constraintParameterId) REFERENCES ConstraintParameter(id)\n" +
                ");\n" +
                "\n" +
                "-- ModelMetadata Table\n" +
                "CREATE TABLE ModelMetadata (\n" +
                "    modelId INTEGER PRIMARY KEY,\n" +
                "    probabilities BOOLEAN,\n" +
                "    featureImportances BOOLEAN,\n" +
                "    decisionTree BOOLEAN,\n" +
                "    ensembleType VARCHAR(255),\n" +
                "    family VARCHAR(255),\n" +
                "    FOREIGN KEY (modelId) REFERENCES Model(id)\n" +
                ");\n" +
                "\n" +
                "-- ParameterValue Table\n" +
                "CREATE TABLE ParameterValue (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    parameterId INTEGER,\n" +
                "    value VARCHAR(255),\n" +
                "    FOREIGN KEY (parameterId) REFERENCES Parameter(id)\n" +
                ");\n" +
                "\n" +
                "-- IncompatibleMetric Table\n" +
                "CREATE TABLE IncompatibleMetric (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    modelId INTEGER,\n" +
                "    metricName VARCHAR(255),\n" +
                "    FOREIGN KEY (modelId) REFERENCES Model(id)\n" +
                ");\n";
    }

    public void insertDataScripts() {
        ObjectMapper mapper = new ObjectMapper();
        Set<String> allModelTypes = new HashSet<>();
        processModelsInDirectory(mapper, Paths.get(JSON_DIR_PATH),
                models -> allModelTypes.addAll(getModelTypes(models)));
        String insertModelTypeScript = buildInsertModelTypeSQL(allModelTypes);
        writeToFile(Paths.get(SQL_DIR_PATH, "insert_model_type.sql").toString(), insertModelTypeScript);
        writeToFile(Paths.get(SQL_DIR_PATH, "insert_model_group.sql").toString(), buildInsertModelGroupSQL());
        logger.info(allModelTypes.toString());
        processModelsInDirectory(mapper, Paths.get(JSON_DIR_PATH), models -> createModelSqlFile(mapper, models));
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

    private void createModelSqlFile(ObjectMapper mapper, Models models) {
        String mltask = models.getModels().get(0).getMlTask();
        String sqlScript = buildInsertSQL(models);
        writeToFile(Paths.get(SQL_DIR_PATH, String.format("insert_models_%s.sql", mltask)).toString(), sqlScript);
        logger.info(mltask + " sql file created successfully!");
    }

    static Set<String> getModelTypes(Models models) {
        Set<String> modelTypes = new HashSet<>();
        for (Model model : models.getModels()) {
            List<String> modelType = model.getParameter().getMetadata().getModelType();
            if (modelType != null && !modelType.isEmpty()) {
                modelTypes.add(modelType.get(0));
            }
        }
        return modelTypes;
    }

    static String buildInsertModelTypeSQL(Set<String> modelTypes) {
        StringBuilder sb = new StringBuilder();
        for (String modelType : modelTypes) {
            sb.append("INSERT INTO ModelType(name) VALUES ('").append(modelType).append("');\n");
        }
        return sb.toString();
    }

    static String buildInsertModelGroupSQL() {
        StringBuilder sb = new StringBuilder();
        Properties properties = PropertiesConfig.getProperties();
        String groupsProperty = properties.getProperty("groups");
        List<String> groups = Arrays.asList(groupsProperty.split(","));

        for (String group : groups) {
            sb.append("INSERT INTO ModelGroup(name) VALUES ('").append(group).append("');\n");
        }
        return sb.toString();
    }

    static String buildInsertSQL(Models models) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<EnsembleFamily> ensembleFamilies = null;
        try {
            ensembleFamilies = objectMapper.readValue(Paths.get("static", "esemble-family.json").toFile(), new TypeReference<List<EnsembleFamily>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (Model model : models.getModels()) {
            String name = model.getName().replace("'", "''");
            String mlTask = model.getMlTask().replace("'", "''");
            Metadata metadata = model.getParameter().getMetadata();
            metadata.getAdvantages().replaceAll(s -> s.replace("'", "''"));
            metadata.getDisadvantages().replaceAll(s -> s.replace("'", "''"));
            String modelDescription = metadata.getModelDescription().replace("'", "''");
            String advantagesArray = "{" + String.join(",", metadata.getAdvantages()) + "}";
            String disadvantagesArray = "{" + String.join(",", metadata.getDisadvantages()) + "}";

            sb.append("INSERT INTO Model(name, mlTask, description, displayName, structure, advantages, disadvantages, isEnabled, modelTypeId) VALUES ('").append(name).append("', '").append(mlTask).append("', '").append(modelDescription).append("', '").append(metadata.getDisplayName()).append("', '").append(metadata.getStructure()).append("', '").append(advantagesArray).append("', '")      // Use the transformed array string
                    .append(disadvantagesArray).append("',")     // Use the transformed array string
                    .append(model.isBlackListed()).append(",").append("(select id from ModelType where name='").append(metadata.getModelType().get(0)).append("'));\n");

            for (String dep : metadata.getDeps()) {
                sb.append("INSERT INTO ModelDependency(modelId, name) VALUES ((select id from Model where name='").append(name).append("' and mlTask='").append(mlTask).append("'),").append("'").append(dep).append("');\n");
            }

            for (String group : model.getGroups()) {
                sb.append("INSERT INTO ModelToGroup(modelId, groupId) VALUES ((select id from Model where name='").append(name).append("' and mlTask='").append(mlTask).append("'),").append("(select id from ModelGroup where name='").append(group).append("'));\n");
            }

            List<String> prime = Optional.ofNullable(metadata.getPrime()).orElse(Collections.emptyList());

            List<InputParameter> parametersInPrime = new ArrayList<>();
            List<InputParameter> parametersNotInPrime = new ArrayList<>();

            for (InputParameter parameter : model.getParameter().getInputParameters()) {
                if (prime.contains(parameter.getParameterName())) {
                    parametersInPrime.add(parameter);
                } else {
                    parametersNotInPrime.add(parameter);
                }
            }

            List<InputParameter> orderedParameters = new ArrayList<>();
            orderedParameters.addAll(parametersInPrime);
            orderedParameters.addAll(parametersNotInPrime);

            int count = 0;
            for (InputParameter parameter : orderedParameters) {
                String description = parameter.getDescription().replace("'", "''");
                count++; // Add a counter to track the order
                sb.append("INSERT INTO Parameter(parameterType, name, minValue, maxValue, defaultValue, label, description, enabled, hasConstraint, constraintInformation, fixedValue, ordering, modelId) VALUES ('").append(parameter.getParameterType()).append("', '").append(parameter.getParameterName()).append("', '").append(parameter.getMinValue()).append("', '").append(parameter.getMaxValue()).append("', '")
                        //TODO default value depends on the parameter type need to fix
                        .append(parameter.getDefaultValue()).append("',").append("'").append(parameter.getLabel()).append("',").append("'").append(description).append("',").append(parameter.isEnabled()).append(",").append(parameter.isConstraint()).append(",").append("'").append(parameter.getConstraintInformation()).append("',").append(parameter.isFixedValue()).append(",").append(count).append(",").append("(select id from Model where name='").append(name).append("' and mlTask='").append(mlTask).append("'));\n");

                for (String value : parameter.getValues()) {
                    value = value.replace("'", "''");
                    sb.append("INSERT INTO ParameterValue(value,parameterId) VALUES ('").append(value).append("',(select id from Parameter where name='").append(parameter.getParameterName()).append("' and modelId=(select id from model where name='").append(name).append("' and mlTask='").append(mlTask).append("')));\n");
                }
            }

            EnsembleFamily matchingFamily = null;
            for (EnsembleFamily family : ensembleFamilies) {
                if (family.getName().equals(model.getName())) {
                    matchingFamily = family;
                    break;
                }
            }

            if (matchingFamily != null) {
                sb.append("INSERT INTO ModelMetadata(modelId, probabilities, featureImportances, decisionTree, ensembleType, family) VALUES ((select id from Model where name='").append(name).append("' and mlTask='").append(mlTask).append("'),").append(metadata.getSupports().getProbabilities()).append(",").append(metadata.getSupports().getFeatureImportances()).append(",").append(metadata.getSupports().getDecisionTree()).append(",").append("'").append(matchingFamily.getEnsembleType()).append("',").append("'").append(matchingFamily.getFamily()).append("');\n");
            }

            for (String metric : model.getIncompatibleMetrics()) {
                sb.append("INSERT INTO IncompatibleMetric(modelId, metricName) VALUES ((select id from Model where name='").append(name).append("' and mlTask='").append(mlTask).append("'),").append("'").append(metric).append("');\n");
            }
        }


        return sb.toString();
    }
}
