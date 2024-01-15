package scripts.inserts;

import static scripts.inserts.model.InsertModelTable.*;
import static scripts.inserts.parameters.InsertConstraintsTables.*;
import static scripts.inserts.parameters.InsertParametersTables.*;

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
    String num;
    if (mltask.equals("classification")) {
      num = "12";
    } else if (mltask.equals("forecasting")) {
      num = "13";
    } else {
      num = "14";
    }
    FileUtils.writeToFile(
        Paths.get(SQL_DIR_PATH, String.format("%s-DML-insert_models_%s.sql",num, mltask)).toString(),
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
    //    sb.append(buildInsertConstraintSQL(model));
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

  public static List<ParameterTypeDistribution> getParameterTypeDistributionList(
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

  private static String buildInsertRestParameterTablesSQL(Model model) {
    StringBuilder sb = new StringBuilder();
    for (HyperParameter parameter : model.getHyperParameters()) {
      Object defaultValue = parameter.getDefaultValue();
      List<ParameterTypeDistribution> parameterTypes = getParameterTypeDistributionList(parameter);
      for (ParameterTypeDistribution parameterType : parameterTypes) {
        String subQuery = buildParameterTypeDefinitionQuery(model, parameter, parameterType);

        if (parameterType.getParameterType().equals("categorical")) {
          appendCategoricalParameterSQL(sb, defaultValue, subQuery);
          for (Object value : parameter.getDomain().getCategoricalSet().getCategories()) {
            appendCategoricalParameterValueSQL(sb, value, subQuery);
          }
        } else if (parameterType.getParameterType().equals("float")) {
          appendFloatParameterSQL(sb, defaultValue, subQuery);
          for (Interval interval : parameter.getDomain().getFloatSet().getIntervals()) {
            appendFloatParameterRangeSQL(sb, interval, subQuery);
          }
        } else if (parameterType.getParameterType().equals("integer")) {
          appendIntegerParameterSQL(sb, defaultValue, subQuery);
          for (Range range : parameter.getDomain().getIntegerSet().getRanges()) {
            appendIntegerParameterRangeSQL(sb, range, subQuery);
          }
        } else if (parameterType.getParameterType().equals("boolean")) {
          appendBooleanParameterSQL(sb, defaultValue, subQuery);
        }
      }
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
}
