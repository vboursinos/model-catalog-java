package scripts.inserts.parameters;

import java.util.UUID;
import model.*;

public class InsertConstraintsTables {

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

  public static String buildInsertConstraintEdgeQuery(
      UUID uuid, ConstraintEdge constraint, Model model) {
    return String.format(
        "INSERT INTO constraint_edge(id, source_parameter_id,target_parameter_id) VALUES ('%s',"
            + "(SELECT id FROM parameter WHERE name = '%s' AND model_id = (SELECT id FROM model WHERE name = '%s')),"
            + "(SELECT id FROM parameter WHERE name = '%s' AND model_id = (SELECT id FROM model WHERE name = '%s')));",
        uuid, constraint.getSource(), model.getName(), constraint.getTarget(), model.getName());
  }

  public static String buildInsertMappingQuery(
      UUID uuid, UUID constraintUuid, String constraintParam, Domain domain, Model model) {
    return String.format(
        "INSERT INTO mapping(id,constraint_id,parameter_type_definition_id) VALUES ('%s','%s',"
            + "(SELECT id FROM parameter_type_definition WHERE parameter_id = (SELECT id FROM parameter WHERE name = '%s' AND model_id = (SELECT id FROM model WHERE name = '%s')) and parameter_type_id = (SELECT id FROM parameter_type WHERE name = '%s')));",
        uuid, constraintUuid, constraintParam, model.getName(), getParameterType(domain));
  }

  public static String generateConstraintValueQuery(
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

  public static String buildCategoricalConstraintValueQuery(Domain domain, UUID uuid) {
    StringBuilder sb = new StringBuilder();
    for (Object value : domain.getCategoricalSet().getCategories()) {
      sb.append(
          String.format(
              "INSERT INTO categorical_constraint_value(mapping_id,value) VALUES ('%s','%s');",
              uuid, value));
    }
    return sb.toString();
  }

  public static String buildIntegerConstraintRangeQuery(Domain domain, UUID uuid) {
    StringBuilder sb = new StringBuilder();
    for (Range range : domain.getIntegerSet().getRanges()) {
      sb.append(
          String.format(
              "INSERT INTO integer_constraint_range(mapping_id,lower,upper) VALUES ('%s',%d,%d);",
              uuid, range.getStart(), range.getStop()));
    }
    return sb.toString();
  }

  public static String buildFloatConstraintRangeQuery(Domain domain, UUID uuid) {
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

  public static String buildBooleanConstraintValueQuery(Domain domain, UUID uuid) {
    StringBuilder sb = new StringBuilder();
    for (Object value : domain.getCategoricalSet().getCategories()) {
      sb.append(
          String.format(
              "INSERT INTO boolean_constraint_value(mapping_id,value) VALUES ('%s',%s);",
              uuid, value));
    }
    return sb.toString();
  }
}
