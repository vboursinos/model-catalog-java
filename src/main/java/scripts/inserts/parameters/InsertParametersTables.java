package scripts.inserts.parameters;

import static scripts.inserts.InsertDynamicTables.getParameterTypeDistributionList;

import java.util.List;
import model.*;

public class InsertParametersTables {

  public static String insertParameterSQL(
      HyperParameter parameter, String description, int count, String name) {
    return String.format(
        "INSERT INTO parameter(name, label, description, enabled, fixed_value, ordering, model_id) VALUES ('%s', '%s', '%s', %s, %s, %d, (select id from Model where name='%s'));\n",
        parameter.getName(),
        parameter.getLabel(),
        description,
        parameter.getEnabled(),
        parameter.isFixedValue(),
        count,
        name);
  }

  public static String buildInsertIntoParameterTypeDefinitionSQL(Model model) {
    StringBuilder sb = new StringBuilder();

    for (HyperParameter parameter : model.getHyperParameters()) {
      List<ParameterTypeDistribution> parameterTypes = getParameterTypeDistributionList(parameter);
      int count = 0;

      for (ParameterTypeDistribution parameterType : parameterTypes) {
        sb.append(
            String.format(
                "INSERT INTO parameter_type_definition(parameter_id, parameter_type_id, "
                    + "parameter_distribution_type_id, ordering) VALUES ((select id from parameter where name='%s' "
                    + "and model_id=(select id from model where name='%s')), (select id from parameter_type where name='%s'), "
                    + "(select id from parameter_distribution_type where name='%s'), %d);\n",
                parameter.getName(),
                model.getName(),
                parameterType.getParameterType(),
                parameterType.getParameterDistribution(),
                count++));
      }
    }
    return sb.toString();
  }

  public static String buildParameterTypeDefinitionQuery(
      Model model, HyperParameter parameter, ParameterTypeDistribution parameterType) {
    String subQuery =
        String.format(
            "(select id from parameter_type_definition where parameter_id=(select id from parameter where name='%s' "
                + "and model_id=(select id from model where name='%s') and parameter_type_id=(select id from parameter_type where name='%s')))",
            parameter.getName(), model.getName(), parameterType.getParameterType());
    return subQuery;
  }

  public static void appendCategoricalParameterSQL(
      StringBuilder sb, Object defaultValue, String subQuery) {
    String strDefaultValue = null;
    if (defaultValue instanceof String) {
      strDefaultValue = (String) defaultValue;
    }

    String insertSQL =
        strDefaultValue != null
            ? String.format(
                "INSERT INTO categorical_parameter(parameter_type_definition_id, default_value) VALUES (%s, '%s');\n",
                subQuery, strDefaultValue)
            : String.format(
                "INSERT INTO categorical_parameter(parameter_type_definition_id, default_value) VALUES (%s, %s);\n",
                subQuery, strDefaultValue);
    sb.append(insertSQL);
  }

  public static void appendCategoricalParameterValueSQL(
      StringBuilder sb, Object value, String subQuery) {
    String insertSQL =
        String.format(
            "INSERT INTO categorical_parameter_value(parameter_type_definition_id, value) VALUES (%s, '%s');\n",
            subQuery, value);
    sb.append(insertSQL);
  }

  public static void appendFloatParameterSQL(
      StringBuilder sb, Object defaultValue, String subQuery) {
    Float floatDefaultValue = null;
    if (defaultValue instanceof Float) {
      floatDefaultValue = (Float) defaultValue;
    } else if (defaultValue instanceof Double) {
      floatDefaultValue = ((Double) defaultValue).floatValue();
    }
    String insertSQL =
        String.format(
            "INSERT INTO float_parameter(parameter_type_definition_id, default_value) VALUES (%s, %f);\n",
            subQuery, floatDefaultValue);
    sb.append(insertSQL);
  }

  public static void appendFloatParameterRangeSQL(
      StringBuilder sb, Interval interval, String subQuery) {
    String insertSQL =
        String.format(
            "INSERT INTO float_parameter_range(parameter_type_definition_id, is_left_open, is_right_open, lower, upper) VALUES (%s, %s, %s, %f, %f);\n",
            subQuery,
            interval.getLeft(),
            interval.getRight(),
            interval.getLower(),
            interval.getUpper());
    sb.append(insertSQL);
  }

  public static void appendIntegerParameterSQL(
      StringBuilder sb, Object defaultValue, String subQuery) {
    Integer intDefaultValue = null;
    if (defaultValue instanceof Integer) {
      intDefaultValue = (Integer) defaultValue;
    }
    String insertSQL =
        String.format(
            "INSERT INTO integer_parameter(parameter_type_definition_id, default_value) VALUES (%s, %d);\n",
            subQuery, intDefaultValue);
    sb.append(insertSQL);
  }

  public static void appendIntegerParameterRangeSQL(
      StringBuilder sb, Range range, String subQuery) {
    String insertSQL =
        String.format(
            "INSERT INTO integer_parameter_value(parameter_type_definition_id, lower, upper) VALUES (%s, %d, %d);\n",
            subQuery, range.getStart(), range.getStop());
    sb.append(insertSQL);
  }

  public static void appendBooleanParameterSQL(
      StringBuilder sb, Object defaultValue, String subQuery) {
    Boolean boolDefaultValue = null;
    if (defaultValue instanceof Boolean) {
      boolDefaultValue = (Boolean) defaultValue;
    }
    String insertSQL =
        String.format(
            "INSERT INTO boolean_parameter(parameter_type_definition_id, default_value) VALUES (%s, %b);\n",
            subQuery, boolDefaultValue);
    sb.append(insertSQL);
  }
}
