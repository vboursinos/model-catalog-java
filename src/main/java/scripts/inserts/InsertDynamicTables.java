package scripts.inserts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.FileUtils;

public class InsertDynamicTables {
  private static final Logger logger = LogManager.getLogger(InsertDynamicTables.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final String JSON_DIR_PATH = "model_infos";
  private static final String SQL_DIR_PATH = "sql_scripts";
  private static final String ENSEMBLE_FAMILIES_FILE = "ensemble-family.json";
  private static final String ENSEMBLE_FAMILIES_DIR = "static";

  public void insertDataScripts() {
    ObjectMapper mapper = new ObjectMapper();
    Path dirPath = Paths.get(JSON_DIR_PATH);
    processModelsInDirectory(mapper, dirPath, this::createModelSqlFile);
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

  private void createModelSqlFile(Models models) {
    String mltask = models.getModels().get(0).getMlTask();
    String sqlScript = buildInsertSQL(models);
    FileUtils.writeToFile(
        Paths.get(SQL_DIR_PATH, String.format("insert_models_%s.sql", mltask)).toString(),
        sqlScript);
    logger.info(mltask + " sql file created successfully!");
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
      ensembleFamilies =
          objectMapper.readValue(
              Paths.get(ENSEMBLE_FAMILIES_DIR, ENSEMBLE_FAMILIES_FILE).toFile(),
              new TypeReference<List<EnsembleFamily>>() {});
    } catch (IOException e) {
      e.printStackTrace();
    }
    return ensembleFamilies;
  }

  private static void processModel(
      Model model, List<EnsembleFamily> ensembleFamilies, StringBuilder sb) {
    String name = model.getName().replace("'", "''");
    Metadata metadata = model.getMetadata();
    replaceSingleQuotesInMetadata(metadata);

    sb.append(buildInsertIntoModelSQL(model, ensembleFamilies));
    sb.append(buildInsertIntoModelToGroupSQL(name, model.getGroups()));
    sb.append(buildInsertIntoParameterAndParameterValueSQL(model));
    sb.append(buildInsertIntoIncompatibleMetricSQL(model));
    sb.append(buildInsertIntoParameterTypeDefinitionSQL(model));
    sb.append(buildInsertRestParameterTablesSQL(model));
    sb.append(buildInsertConstraintSQL(model));
  }

  private static String buildInsertConstraintSQL(Model model) {
    StringBuilder sb = new StringBuilder();
    for (ConstraintEdge constraint : model.getConstraintEdges()) {
      for (Item item : constraint.getMapping()) {
        UUID uuid = UUID.randomUUID();
        sb.append(buildInsertConstraintEdgeQuery(uuid, constraint, model));
        UUID mappingSourceUuid = UUID.randomUUID();
        UUID mappingTargetUuid = UUID.randomUUID();
        sb.append(
            buildInsertMappingQuery(
                mappingSourceUuid, uuid, constraint.getSource(), item.getSource(), model));
        sb.append(
            buildInsertMappingQuery(
                mappingTargetUuid, uuid, constraint.getTarget(), item.getTarget(), model));
        sb.append(generateConstraintValueQuery(item, mappingSourceUuid, mappingTargetUuid));
      }
    }
    return sb.toString();
  }

  private static String buildInsertConstraintEdgeQuery(
      UUID uuid, ConstraintEdge constraint, Model model) {
    return String.format(
        "INSERT INTO constraint_edge(id, source_parameter_id,target_parameter_id) VALUES ('%s',"
            + "(SELECT id FROM parameter WHERE name = '%s' AND model_id = (SELECT id FROM model WHERE name = '%s')),"
            + "(SELECT id FROM parameter WHERE name = '%s' AND model_id = (SELECT id FROM model WHERE name = '%s')));",
        uuid, constraint.getSource(), model.getName(), constraint.getTarget(), model.getName());
  }

  private static String buildInsertMappingQuery(
      UUID uuid, UUID constraintUuid, String constraintParam, Domain domain, Model model) {
    return String.format(
        "INSERT INTO mapping(id,constraint_id,parameter_type_definition_id) VALUES ('%s','%s',"
            + "(SELECT id FROM parameter_type_definition WHERE parameter_id = (SELECT id FROM parameter WHERE name = '%s' AND model_id = (SELECT id FROM model WHERE name = '%s')) and parameter_type_id = (SELECT id FROM parameter_type WHERE name = '%s')));",
        uuid, constraintUuid, constraintParam, model.getName(), getParameterType(domain));
  }

  private static String generateConstraintValueQuery(
      Item item, UUID mappingSourceUuid, UUID mappingTargetUuid) {
    StringBuilder sb = new StringBuilder();
    String sourceType = getParameterType(item.getSource());
    String targetType = getParameterType(item.getTarget());
    if ("categorical".equals(sourceType)) {
      sb.append(buildCategoricalConstraintValueQuery(item.getSource(), mappingSourceUuid));
    }
    if ("categorical".equals(targetType)) {
      sb.append(buildCategoricalConstraintValueQuery(item.getTarget(), mappingTargetUuid));
    }
    if ("integer".equals(sourceType)) {
      sb.append(buildIntegerConstraintRangeQuery(item.getSource(), mappingSourceUuid));
    }
    if ("integer".equals(targetType)) {
      sb.append(buildIntegerConstraintRangeQuery(item.getTarget(), mappingTargetUuid));
    }
    if ("float".equals(sourceType)) {
      sb.append(buildFloatConstraintRangeQuery(item.getSource(), mappingSourceUuid));
    }
    if ("float".equals(targetType)) {
      sb.append(buildFloatConstraintRangeQuery(item.getTarget(), mappingTargetUuid));
    }
    if ("boolean".equals(sourceType)) {
      sb.append(buildBooleanConstraintValueQuery(item.getSource(), mappingSourceUuid));
    }
    if ("boolean".equals(targetType)) {
      sb.append(buildBooleanConstraintValueQuery(item.getTarget(), mappingTargetUuid));
    }
    return sb.toString();
  }

  private static String buildCategoricalConstraintValueQuery(Domain domain, UUID uuid) {
    StringBuilder sb = new StringBuilder();
    for (Object value : domain.getCategoricalSet().getCategories()) {
      sb.append(
          String.format(
              "INSERT INTO categorical_constraint_value(mapping_id,value) VALUES ('%s','%s');",
              uuid, value));
    }
    return sb.toString();
  }

  private static String buildIntegerConstraintRangeQuery(Domain domain, UUID uuid) {
    StringBuilder sb = new StringBuilder();
    for (Range range : domain.getIntegerSet().getRanges()) {
      sb.append(
          String.format(
              "INSERT INTO integer_constraint_range(mapping_id,lower,upper) VALUES ('%s',%d,%d);",
              uuid, range.getStart(), range.getStop()));
    }
    return sb.toString();
  }

  private static String buildFloatConstraintRangeQuery(Domain domain, UUID uuid) {
    StringBuilder sb = new StringBuilder();
    for (Interval interval : domain.getFloatSet().getIntervals()) {
      sb.append(
          String.format(
              "INSERT INTO float_constraint_range(mapping_id,is_left_open,is_right_open,lower,upper) VALUES ('%s',%s,%s,%f,%s);",
              uuid,
              interval.getLeft(),
              interval.getRight(),
              interval.getLower(),
              interval.getUpper()));
    }
    return sb.toString();
  }

  private static String buildBooleanConstraintValueQuery(Domain domain, UUID uuid) {
    StringBuilder sb = new StringBuilder();
    for (Object value : domain.getCategoricalSet().getCategories()) {
      sb.append(
          String.format(
              "INSERT INTO boolean_constraint_value(mapping_id,value) VALUES ('%s',%s);",
              uuid, value));
    }
    return sb.toString();
  }

  private static List<ParameterTypeDistribution> getParameterTypeDistributionList(
      HyperParameter parameter) {
    List<ParameterTypeDistribution> parameterTypes = new ArrayList<>();
    Domain domain = parameter.getDomain();

    if (domain.getCategoricalSet() != null
        && !domain.getCategoricalSet().getCategories().isEmpty()) {
      String distributionType =
          (parameter.getDefaultValue().equals(true) || parameter.getDefaultValue().equals(false))
              ? "boolean"
              : "categorical";
      parameterTypes.add(new ParameterTypeDistribution(distributionType, "uniform"));
    }

    if (domain.getFloatSet() != null && !domain.getFloatSet().getIntervals().isEmpty()) {
      parameterTypes.add(
          new ParameterTypeDistribution(
              "float", parameter.getDistribution().getFloatDistribution()));
    }

    if (domain.getIntegerSet() != null && !domain.getIntegerSet().getRanges().isEmpty()) {
      parameterTypes.add(
          new ParameterTypeDistribution(
              "integer", parameter.getDistribution().getIntegerDistribution()));
    }
    return parameterTypes;
  }

  private static String getParameterType(Domain domain) {
    String parameterType = null;
    if (domain.getCategoricalSet() != null
        && !domain.getCategoricalSet().getCategories().isEmpty()) {
      if (domain.getCategoricalSet().getCategories().contains("True")
          || domain.getCategoricalSet().getCategories().contains("False")) {
        parameterType = "boolean";
      } else {
        parameterType = "categorical";
      }
    }

    if (domain.getFloatSet() != null && !domain.getFloatSet().getIntervals().isEmpty()) {
      parameterType = "float";
    }

    if (domain.getIntegerSet() != null && !domain.getIntegerSet().getRanges().isEmpty()) {
      parameterType = "integer";
    }
    return parameterType;
  }

  private static String buildInsertIntoParameterTypeDefinitionSQL(Model model) {
    StringBuilder sb = new StringBuilder();

    for (HyperParameter parameter : model.getHyperParameters()) {
      List<ParameterTypeDistribution> parameterTypes = getParameterTypeDistributionList(parameter);
      int count = 0;

      for (ParameterTypeDistribution parameterType : parameterTypes) {
        sb.append(
                "INSERT INTO parameter_type_definition(parameter_id, parameter_type_id, parameter_distribution_type_id, ordering) VALUES ((select id from parameter where name='")
            .append(parameter.getName())
            .append("'  and model_id=(select id from model where name='")
            .append(model.getName())
            .append("')), (select id from parameter_type where name='")
            .append(parameterType.getParameterType())
            .append("'), ")
            .append("(select id from parameter_distribution_type where name='")
            .append(parameterType.getParameterDistribution())
            .append("'), ")
            .append(count)
            .append(");\n");
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
          if (defaultValue instanceof String) {
            strDefaultValue = (String) defaultValue;
          }
          sb.append(
                  "INSERT INTO categorical_parameter(parameter_type_definition_id, default_value) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='")
              .append(parameter.getName())
              .append("'  and model_id=(select id from model where name='")
              .append(model.getName())
              .append("') and parameter_type_id=(select id from parameter_type where name='")
              .append(parameterType.getParameterType())
              .append("'))), ");
          if (strDefaultValue != null) {
            sb.append("'").append(strDefaultValue).append("'");
          } else {
            sb.append(strDefaultValue);
          }
          sb.append(");\n");
          for (Object value : parameter.getDomain().getCategoricalSet().getCategories()) {
            sb.append(
                    "INSERT INTO categorical_parameter_value(parameter_type_definition_id, value ) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='")
                .append(parameter.getName())
                .append("'  and model_id=(select id from model where name='")
                .append(model.getName())
                .append("') and parameter_type_id=(select id from parameter_type where name='")
                .append(parameterType.getParameterType())
                .append("'))), '")
                .append(value)
                .append("');\n");
          }
        }
        if (parameterType.getParameterType().equals("float")) {
          Float floatDefaultValue = null;
          if (defaultValue instanceof Float) {
            floatDefaultValue = (Float) defaultValue;
          } else if (defaultValue instanceof Double) {
            floatDefaultValue = ((Double) defaultValue).floatValue();
          }
          sb.append(
                  "INSERT INTO float_parameter(parameter_type_definition_id, default_value) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='")
              .append(parameter.getName())
              .append("'  and model_id=(select id from model where name='")
              .append(model.getName())
              .append("') and parameter_type_id=(select id from parameter_type where name='")
              .append(parameterType.getParameterType())
              .append("'))), ")
              .append(floatDefaultValue)
              .append(");\n");
          for (Interval interval : parameter.getDomain().getFloatSet().getIntervals()) {
            sb.append(
                    "INSERT INTO float_parameter_range(parameter_type_definition_id, is_left_open, is_right_open, lower, upper ) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='")
                .append(parameter.getName())
                .append("'  and model_id=(select id from model where name='")
                .append(model.getName())
                .append("') and parameter_type_id=(select id from parameter_type where name='")
                .append(parameterType.getParameterType())
                .append("'))), ")
                .append(interval.getLeft())
                .append(", ")
                .append(interval.getRight())
                .append(", ")
                .append(interval.getLower())
                .append(", ")
                .append(interval.getUpper())
                .append(");\n");
          }
        }
        if (parameterType.getParameterType().equals("integer")) {
          Integer intDefaultValue = null;
          if (defaultValue instanceof Integer) {
            intDefaultValue = (Integer) defaultValue;
          }
          sb.append(
                  "INSERT INTO integer_parameter(parameter_type_definition_id, default_value) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='")
              .append(parameter.getName())
              .append("'  and model_id=(select id from model where name='")
              .append(model.getName())
              .append("') and parameter_type_id=(select id from parameter_type where name='")
              .append(parameterType.getParameterType())
              .append("'))), ")
              .append(intDefaultValue)
              .append(");\n");
          for (Range range : parameter.getDomain().getIntegerSet().getRanges()) {
            sb.append(
                    "INSERT INTO integer_parameter_range(parameter_type_definition_id, start, stop ) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='")
                .append(parameter.getName())
                .append("'  and model_id=(select id from model where name='")
                .append(model.getName())
                .append("') and parameter_type_id=(select id from parameter_type where name='")
                .append(parameterType.getParameterType())
                .append("'))), ")
                .append(range.getStart())
                .append(", ")
                .append(range.getStop())
                .append(");\n");
          }
        }
        if (parameterType.getParameterType().equals("boolean")) {
          Boolean boolDefaultValue = null;
          if (defaultValue instanceof Boolean) {
            boolDefaultValue = (Boolean) defaultValue;
          }
          sb.append(
                  "INSERT INTO boolean_parameter(parameter_type_definition_id, default_value) VALUES ((select id from parameter_type_definition where parameter_id=(select id from parameter where name='")
              .append(parameter.getName())
              .append("'  and model_id=(select id from model where name='")
              .append(model.getName())
              .append("') and parameter_type_id=(select id from parameter_type where name='")
              .append(parameterType.getParameterType())
              .append("'))), ")
              .append(boolDefaultValue)
              .append(");\n");
        }
      }
    }
    return sb.toString();
  }

  private static String buildInsertIntoIncompatibleMetricSQL(Model model) {
    StringBuilder sb = new StringBuilder();
    for (String metric : model.getIncompatibleMetrics()) {
      sb.append(
              "INSERT INTO incompatible_metric(model_id, metric_id) VALUES ((select id from model where name='")
          .append(model.getName())
          .append("'), (select id from metric where name='")
          .append(metric)
          .append("'));\n");
    }
    return sb.toString();
  }

  private static void replaceSingleQuotesInMetadata(Metadata metadata) {
    List<String> advantages =
        metadata.getAdvantages().stream()
            .map(s -> s.replace("'", "''"))
            .collect(Collectors.toList());
    metadata.setAdvantages(advantages);

    List<String> disadvantages =
        metadata.getDisadvantages().stream()
            .map(s -> s.replace("'", "''"))
            .collect(Collectors.toList());
    metadata.setDisadvantages(disadvantages);

    String modelDescription = metadata.getModelDescription().replace("'", "''");
    metadata.setModelDescription(modelDescription);
  }

  private static String buildInsertIntoModelSQL(
      Model model, List<EnsembleFamily> ensembleFamilies) {
    StringBuilder sb = new StringBuilder();

    String advantagesArray = "{" + String.join(",", model.getMetadata().getAdvantages()) + "}";
    String disadvantagesArray =
        "{" + String.join(",", model.getMetadata().getDisadvantages()) + "}";

    String ensembleType = null;
    String familyType = null;
    for (EnsembleFamily ensembleFamily : ensembleFamilies) {
      if (ensembleFamily.getName().equals(model.getName())) {
        ensembleType = ensembleFamily.getEnsembleType();
        familyType = ensembleFamily.getFamily();
        break;
      }
    }

    sb.append(
            "INSERT INTO model(name, ml_task_id, description, display_name, structure_id, advantages, disadvantages, enabled, ensemble_type_id, family_type_id, decision_tree, model_type_id) VALUES ('")
        .append(model.getName())
        .append("', ")
        .append("(select id from ml_task where name='")
        .append(model.getMlTask())
        .append("'),'")
        .append(model.getMetadata().getModelDescription())
        .append("', '")
        .append(model.getMetadata().getDisplayName())
        .append("', ")
        .append("(select id from model_structure_type where name='")
        .append(model.getMetadata().getStructure())
        .append("'), '")
        .append(advantagesArray)
        .append("', '")
        .append(disadvantagesArray)
        .append("',")
        .append(model.isBlackListed())
        .append(",")
        .append("(select id from ensemble_type where name='")
        .append(ensembleType)
        .append("'),(select id from family_type where name='")
        .append(familyType)
        .append("'),")
        .append(model.getMetadata().getSupports().getDecisionTree())
        .append(",")
        .append("(select id from model_type where name='")
        .append(model.getMetadata().getModelType().get(0))
        .append("'));\n");

    return sb.toString();
  }

  private static String buildInsertIntoModelToGroupSQL(String name, List<String> modelGroups) {
    StringBuilder sb = new StringBuilder();

    for (String group : modelGroups) {
      sb.append(
              "INSERT INTO model_group(model_id, group_id) VALUES ((select id from Model where name='")
          .append(name)
          .append("'),")
          .append("(select id from group_type where name='")
          .append(group)
          .append("'));\n");
    }

    return sb.toString();
  }

  private static String buildInsertIntoParameterAndParameterValueSQL(Model model) {
    StringBuilder sb = new StringBuilder();

    List<String> prime =
        Optional.ofNullable(model.getMetadata().getPrime()).orElse(Collections.emptyList());

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

  private static String insertIntoParameterAndParameterValue(
      List<HyperParameter> orderedParameters, String name) {
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (HyperParameter parameter : orderedParameters) {
      String description = parameter.getDescription().replace("'", "''");
      count++;
      sb.append(insertParameterSQL(parameter, description, count, name));
    }
    return sb.toString();
  }

  private static String insertParameterSQL(
      HyperParameter parameter, String description, int count, String name) {
    return new StringBuilder(
            "INSERT INTO parameter(name, label, description, enabled, fixed_value, ordering, model_id) VALUES ('")
        .append(parameter.getName())
        .append("', '")
        .append(parameter.getLabel())
        .append("',")
        .append("'")
        .append(description)
        .append("',")
        .append(parameter.getEnabled())
        .append(",")
        .append(parameter.isFixedValue())
        .append(",")
        .append(count)
        .append(",")
        .append("(select id from Model where name='")
        .append(name)
        .append("'));\n")
        .toString();
  }
}
