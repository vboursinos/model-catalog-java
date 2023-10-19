package scripts.inserts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.PropertiesConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import model.EnsembleFamily;
import model.Model;
import model.Models;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.FileUtils;

public class InsertStaticTables {
  private static final Logger logger = LogManager.getLogger(InsertStaticTables.class);
  private static final String JSON_DIR_PATH = "model_infos";
  private static final String SQL_DIR_PATH = "sql_scripts";

  public void insertDataScripts() {
    ObjectMapper mapper = new ObjectMapper();

    // Get unique values from JSON files
    Path dirPath = Paths.get(JSON_DIR_PATH);
    Set<String> allModelTypes =
        extractUniqueValues(mapper, dirPath, InsertStaticTables::getModelTypes);
    Set<String> allModelStructureTypes =
        extractUniqueValues(mapper, dirPath, InsertStaticTables::getModelStructureTypes);
    Set<String> allMlTasks = extractUniqueValues(mapper, dirPath, InsertStaticTables::getMlTasks);
    Set<String> allMetrics = extractUniqueValues(mapper, dirPath, InsertStaticTables::getMetrics);

    // Write values to SQL files
    createSQLFile("insert_model_type.sql", buildInsertModelTypeSQL(allModelTypes));
    logger.info("Model type sql file is created successfully");
    createSQLFile("insert_group_type.sql", buildInsertGroupTypeSQL());
    logger.info("Group type sql file is created successfully");
    createSQLFile("insert_parameter_type.sql", buildParameterTypeSQL());
    logger.info("Parameter type sql file is created successfully");
    createSQLFile("insert_distribution.sql", buildDistributionSQL());
    logger.info("Distribution type sql file is created successfully");
    createSQLFile(
        "insert_model_structure_type.sql",
        buildInsertModelStructureTypeSQL(allModelStructureTypes));
    logger.info("Model structure type sql file is created successfully");
    createSQLFile("insert_ml_task.sql", buildInsertMlTaskSQL(allMlTasks));
    logger.info("Ml_task sql file is created successfully");
    createSQLFile("insert_metrics.sql", buildMetricSQL(allMetrics));
    logger.info("Metrics sql file is created successfully");
    createSQLFile("insert_ensemble_family.sql", buildInsertEnsembleFamilyTypeSQL());
    logger.info("Ensemble type and family type sql files are created successfully");
  }

  private void createSQLFile(String fileName, String content) {
    FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, fileName).toString(), content);
  }

  private void processModelsInDirectory(
      ObjectMapper mapper, Path dirPath, Consumer<Models> modelProcessor) {
    try {
      Files.newDirectoryStream(dirPath)
          .forEach(
              filePath -> {
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

  private Set<String> extractUniqueValues(
      ObjectMapper mapper, Path dirPath, Function<Models, Set<String>> valueExtractor) {
    Set<String> uniqueValues = new HashSet<>();
    processModelsInDirectory(
        mapper, dirPath, models -> uniqueValues.addAll(valueExtractor.apply(models)));
    return uniqueValues;
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
      sb.append("INSERT INTO model_structure_type(name) VALUES ('")
          .append(modelStructureType)
          .append("');\n");
    }
    return sb.toString();
  }

  static String buildInsertMlTaskSQL(Set<String> mlTaskSet) {
    StringBuilder sb = new StringBuilder();
    for (String mlTask : mlTaskSet) {
      sb.append("INSERT INTO ml_task_type(name) VALUES ('").append(mlTask).append("');\n");
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

  static String buildInsertEnsembleFamilyTypeSQL() {
    String filePath = Paths.get("static", "ensemble-family.json").toString();
    ObjectMapper objectMapper = new ObjectMapper();
    StringBuilder sb = new StringBuilder();
    try {
      List<EnsembleFamily> modelList =
          objectMapper.readValue(new File(filePath), new TypeReference<List<EnsembleFamily>>() {});
      Set<String> ensembleTypes = new HashSet<>();
      Set<String> familyTypes = new HashSet<>();
      for (EnsembleFamily model : modelList) {
        ensembleTypes.add(model.getEnsembleType());
        familyTypes.add(model.getFamily());
      }
      for (String ensembleType : ensembleTypes) {
        sb.append("INSERT INTO model_ensemble_type(name) VALUES ('").append(ensembleType).append("');\n");
      }
      for (String familyType : familyTypes) {
        sb.append("INSERT INTO model_family_type(name) VALUES ('").append(familyType).append("');\n");
      }
    } catch (IOException e) {
      logger.error("Error reading ensemble-family.json file: " + e.getMessage(), e);
    }
    return sb.toString();
  }

  static String buildInsertGroupTypeSQL() {
    StringBuilder sb = new StringBuilder();
    Properties properties = PropertiesConfig.getProperties();
    String groupsProperty = properties.getProperty("groups");
    List<String> groups = Arrays.asList(groupsProperty.split(","));

    for (String group : groups) {
      sb.append("INSERT INTO model_group_type(name) VALUES ('").append(group).append("');\n");
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
      sb.append("INSERT INTO parameter_distribution_type(name) VALUES ('")
          .append(distributionType)
          .append("');\n");
    }
    return sb.toString();
  }
}
