package scripts.create_tables;

import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.FileUtils;

public class CreateTableSqlScript {
  private static final String SQL_DIR_PATH = "sql_scripts";
  private static final String SETUP_SCRIPT_NAME = "1-DDL-create_initial_set_of_tables.sql";

  private static final Logger logger = LogManager.getLogger(CreateTableSqlScript.class);

  public void createTablesScript() {
    String sqlScript = getSqlScript();
    FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, SETUP_SCRIPT_NAME).toString(), sqlScript);
    logger.info("Create all db tables sql file is created successfully");
  }

  private String getSqlScript() {
    return "-- changeset liquibaseuser:1\n" +
            "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";\n" +
            "-- rollback -- You may not be able to drop the extension, so no rollback specified.\n" +
            "\n" +
            "-- changeset liquibaseuser:2\n" +
            "CREATE OR REPLACE FUNCTION generate_uuid()\n" +
            "RETURNS uuid AS $$\n" +
            "BEGIN\n" +
            "  RETURN gen_random_uuid();\n" +
            "END;\n" +
            "$$ LANGUAGE plpgsql;\n" +
            "-- rollback -- You may not be able to drop the function, so no rollback specified.\n" +
            "\n" +
            "-- changeset liquibaseuser:3\n" +
            "CREATE TABLE model_type (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  name varchar NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE model_type;\n" +
            "\n" +
            "-- changeset liquibaseuser:4\n" +
            "CREATE TABLE ml_task_type (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  name varchar NOT NULL UNIQUE\n" +
            ");\n" +
            "-- rollback DROP TABLE ml_task_type;\n" +
            "\n" +
            "-- changeset liquibaseuser:5\n" +
            "CREATE TABLE model_structure_type (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  name varchar NOT NULL UNIQUE\n" +
            ");\n" +
            "-- rollback DROP TABLE model_structure_type;\n" +
            "\n" +
            "-- changeset liquibaseuser:6\n" +
            "CREATE TABLE model_group_type (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  name varchar NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE model_group_type;\n" +
            "\n" +
            "-- changeset liquibaseuser:7\n" +
            "CREATE TABLE model_ensemble_type (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  name varchar NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE model_ensemble_type;\n" +
            "\n" +
            "-- changeset liquibaseuser:8\n" +
            "CREATE TABLE model_family_type (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  name varchar NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE model_family_type;\n" +
            "\n" +
            "-- changeset liquibaseuser:9\n" +
            "CREATE TABLE model (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  model_type_id uuid REFERENCES model_type (id),\n" +
            "  ml_task_id uuid REFERENCES ml_task_type (id) NOT NULL,\n" +
            "  name varchar NOT NULL UNIQUE,\n" +
            "  display_name varchar NOT NULL,\n" +
            "  structure_id uuid REFERENCES model_structure_type (id) NOT NULL,\n" +
            "  description varchar,\n" +
            "  advantages text[],\n" +
            "  disadvantages text[],\n" +
            "  enabled boolean NOT NULL,\n" +
            "  ensemble_type_id uuid REFERENCES model_ensemble_type (id),\n" +
            "  family_type_id uuid REFERENCES model_family_type (id),\n" +
            "  decision_tree boolean NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE model;\n" +
            "\n" +
            "-- changeset liquibaseuser:10\n" +
            "CREATE TABLE rel_model__groups (\n" +
            "  model_id uuid REFERENCES model (id),\n" +
            "  group_id uuid REFERENCES model_group_type (id)\n" +
            ");\n" +
            "-- rollback DROP TABLE rel_model__groups;\n" +
            "\n" +
            "-- changeset liquibaseuser:11\n" +
            "CREATE TABLE metric (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  name varchar NOT NULL UNIQUE\n" +
            ");\n" +
            "-- rollback DROP TABLE metric;\n" +
            "\n" +
            "-- changeset liquibaseuser:12\n" +
            "CREATE TABLE rel_model__incompatible_metrics (\n" +
            "  model_id uuid REFERENCES model (id),\n" +
            "  metric_id uuid REFERENCES metric (id)\n" +
            ");\n" +
            "-- rollback DROP TABLE rel_model__incompatible_metrics;\n" +
            "\n" +
            "-- changeset liquibaseuser:13\n" +
            "CREATE TABLE parameter (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  model_id uuid REFERENCES model (id),\n" +
            "  name varchar NOT NULL,\n" +
            "  label varchar NOT NULL,\n" +
            "  description varchar,\n" +
            "  enabled boolean NOT NULL,\n" +
            "  fixed_value boolean NOT NULL,\n" +
            "  ordering integer NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE parameter;\n" +
            "\n" +
            "-- changeset liquibaseuser:14\n" +
            "CREATE TABLE parameter_type (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  name varchar NOT NULL UNIQUE\n" +
            ");\n" +
            "-- rollback DROP TABLE parameter_type;\n" +
            "\n" +
            "-- changeset liquibaseuser:15\n" +
            "CREATE TABLE parameter_distribution_type (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  name varchar NOT NULL UNIQUE\n" +
            ");\n" +
            "-- rollback DROP TABLE parameter_distribution_type;\n" +
            "\n" +
            "-- changeset liquibaseuser:16\n" +
            "CREATE TABLE parameter_type_definition (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  parameter_id uuid REFERENCES parameter (id) NOT NULL,\n" +
            "  parameter_type_id uuid REFERENCES parameter_type (id) NOT NULL,\n" +
            "  parameter_distribution_type_id uuid REFERENCES parameter_distribution_type (id) NOT NULL,\n" +
            "  ordering integer NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE parameter_type_definition;\n" +
            "\n" +
            "-- changeset liquibaseuser:17\n" +
            "CREATE TABLE categorical_parameter (\n" +
            "  id uuid PRIMARY KEY REFERENCES parameter_type_definition (id),\n" +
            "  default_value varchar\n" +
            ");\n" +
            "-- rollback DROP TABLE categorical_parameter;\n" +
            "\n" +
            "-- changeset liquibaseuser:18\n" +
            "CREATE TABLE categorical_parameter_value (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  parameter_type_definition_id uuid REFERENCES categorical_parameter (id) NOT NULL,\n" +
            "  value varchar NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE categorical_parameter_value;\n" +
            "\n" +
            "-- changeset liquibaseuser:19\n" +
            "CREATE TABLE integer_parameter (\n" +
            "  id uuid PRIMARY KEY REFERENCES parameter_type_definition (id),\n" +
            "  default_value integer\n" +
            ");\n" +
            "-- rollback DROP TABLE integer_parameter;\n" +
            "\n" +
            "-- changeset liquibaseuser:20\n" +
            "CREATE TABLE integer_parameter_value (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  parameter_type_definition_id uuid REFERENCES integer_parameter (id) NOT NULL,\n" +
            "  lower integer NOT NULL,\n" +
            "  upper integer NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE integer_parameter_value;\n" +
            "\n" +
            "-- changeset liquibaseuser:21\n" +
            "CREATE TABLE float_parameter (\n" +
            "  id uuid PRIMARY KEY REFERENCES parameter_type_definition (id),\n" +
            "  default_value double precision\n" +
            ");\n" +
            "-- rollback DROP TABLE float_parameter;\n" +
            "\n" +
            "-- changeset liquibaseuser:22\n" +
            "CREATE TABLE float_parameter_range (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  parameter_type_definition_id uuid REFERENCES float_parameter (id) NOT NULL,\n" +
            "  is_left_open boolean NOT NULL,\n" +
            "  is_right_open boolean NOT NULL,\n" +
            "  lower double precision NOT NULL,\n" +
            "  upper double precision NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE float_parameter_range;\n" +
            "\n" +
            "-- changeset liquibaseuser:23\n" +
            "CREATE TABLE boolean_parameter (\n" +
            "  id uuid PRIMARY KEY REFERENCES parameter_type_definition (id),\n" +
            "  default_value boolean\n" +
            ");\n" +
            "-- rollback DROP TABLE boolean_parameter;\n" +
            "\n" +
            "-- changeset liquibaseuser:24\n" +
            "CREATE TABLE constraint_edge (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  source_parameter_id uuid REFERENCES parameter (id) NOT NULL,\n" +
            "  target_parameter_id uuid REFERENCES parameter (id) NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE constraint_edge;\n" +
            "\n" +
            "-- changeset liquibaseuser:25\n" +
            "CREATE TABLE mapping (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  constraint_id uuid REFERENCES constraint_edge (id) NOT NULL,\n" +
            "  parameter_type_definition_id uuid REFERENCES parameter_type_definition (id) NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE mapping;\n" +
            "\n" +
            "-- changeset liquibaseuser:26\n" +
            "CREATE TABLE float_constraint_range (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  mapping_id uuid REFERENCES mapping (id) NOT NULL,\n" +
            "  is_left_open boolean NOT NULL,\n" +
            "  is_right_open boolean NOT NULL,\n" +
            "  lower double precision NOT NULL,\n" +
            "  upper double precision NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE float_constraint_range;\n" +
            "\n" +
            "-- changeset liquibaseuser:27\n" +
            "CREATE TABLE integer_constraint_range (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  mapping_id uuid REFERENCES mapping (id) NOT NULL,\n" +
            "  lower integer NOT NULL,\n" +
            "  upper integer NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE integer_constraint_range;\n" +
            "\n" +
            "-- changeset liquibaseuser:28\n" +
            "CREATE TABLE categorical_constraint_value (\n" +
            "  id uuid DEFAULT generate_uuid() PRIMARY KEY,\n" +
            "  mapping_id uuid REFERENCES mapping (id) NOT NULL,\n" +
            "  value varchar NOT NULL\n" +
            ");\n" +
            "-- rollback DROP TABLE categorical_constraint\n";
  }
}
