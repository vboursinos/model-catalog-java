import utils.FileUtils;

import java.nio.file.Paths;

public class CreateTableSqlScript {
    private static final String SQL_DIR_PATH = "sql_scripts";
    private static final String SETUP_SCRIPT_NAME = "setup.sql";

    public void createTablesScript() {
        String sqlScript = getSqlScript();
        FileUtils.writeToFile(Paths.get(SQL_DIR_PATH, SETUP_SCRIPT_NAME).toString(), sqlScript);
    }

    private String getSqlScript() {
        return  "CREATE TABLE model_type (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  name varchar NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE ml_task (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  name varchar NOT NULL UNIQUE\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE model_structure_type (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  name varchar NOT NULL UNIQUE\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE group_type (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  name varchar NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE model (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  model_type_id integer REFERENCES model_type (id),\n" +
                "  ml_task_id integer REFERENCES ml_task (id) NOT NULL,\n" +
                "  name varchar NOT NULL UNIQUE,\n" +
                "  display_name varchar NOT NULL,\n" +
                "  structure_id integer REFERENCES model_structure_type (id) NOT NULL,\n" +
                "  description varchar,\n" +
                "  advantages text[],\n" +
                "  disadvantages text[],\n" +
                "  enabled boolean NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE model_dependency (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  model_id integer REFERENCES model (id),\n" +
                "  name varchar NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE model_group (\n" +
                "  model_id integer REFERENCES model (id),\n" +
                "  group_id integer REFERENCES group_type (id)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE model_metadata (\n" +
                "  model_id integer REFERENCES model (id),\n" +
                "  decision_tree varchar,\n" +
                "  ensemble_type varchar,\n" +
                "  family varchar\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE metric (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  name varchar NOT NULL UNIQUE\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE incompatible_metric (\n" +
                "  model_id integer REFERENCES model (id),\n" +
                "  metric_id integer REFERENCES metric (id)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE parameter (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  model_id integer REFERENCES model (id),\n" +
                "  name varchar NOT NULL,\n" +
                "  label varchar NOT NULL,\n" +
                "  description varchar,\n" +
                "  enabled boolean NOT NULL,\n" +
                "  fixed_value boolean NOT NULL,\n" +
                "  ordering integer NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE parameter_type (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  name varchar NOT NULL UNIQUE\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE parameter_distribution_type (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  name varchar NOT NULL UNIQUE\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE parameter_type_definition (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  parameter_id integer REFERENCES parameter (id) NOT NULL,\n" +
                "  parameter_type_id integer REFERENCES parameter_type (id) NOT NULL,\n" +
                "  parameter_distribution_type_id integer REFERENCES parameter_distribution_type (id) NOT NULL,\n" +
                "  ordering integer NOT NULL\n" +
                ");\n" +
                "\n" +
                "\n" +
                "CREATE TABLE categorical_parameter (\n" +
                "  parameter_type_definition_id integer REFERENCES parameter_type_definition (id) UNIQUE,\n" +
                "  default_value varchar\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE categorical_parameter_value (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  parameter_type_definition_id integer REFERENCES categorical_parameter (parameter_type_definition_id) NOT NULL,\n" +
                "  value varchar NOT NULL\n" +
                ");\n" +
                "\n" +
                "\n" +
                "CREATE TABLE integer_parameter (\n" +
                "  parameter_type_definition_id integer REFERENCES parameter_type_definition (id) UNIQUE,\n" +
                "  default_value integer\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE integer_parameter_range (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  parameter_type_definition_id integer REFERENCES integer_parameter (parameter_type_definition_id) NOT NULL,\n" +
                "  start integer NOT NULL,\n" +
                "  stop integer NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE float_parameter (\n" +
                "  parameter_type_definition_id integer REFERENCES parameter_type_definition (id) UNIQUE,\n" +
                "  default_value double precision\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE float_parameter_range (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  parameter_type_definition_id integer REFERENCES float_parameter (parameter_type_definition_id) NOT NULL,\n" +
                "  is_left_open boolean NOT NULL,\n" +
                "  is_right_open boolean NOT NULL,\n" +
                "  lower double precision NOT NULL,\n" +
                "  upper double precision NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE boolean_parameter (\n" +
                "  parameter_type_definition_id integer REFERENCES parameter_type_definition (id) UNIQUE,\n" +
                "  default_value boolean\n" +
                ");\n" +
                "\n" +
                "\n" +
                "CREATE TABLE constraint_edge (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  source_parameter_id integer REFERENCES parameter (id) NOT NULL,\n" +
                "  target_parameter_id integer REFERENCES parameter (id) NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE mapping (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  constraint_id integer REFERENCES constraint_edge (id) NOT NULL,\n" +
                "  parameter_type_definition_id integer REFERENCES parameter_type_definition (id) NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE float_contraint_range (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  mapping_id integer REFERENCES mapping (id) NOT NULL,\n" +
                "  is_left_open boolean NOT NULL,\n" +
                "  is_right_open boolean NOT NULL,\n" +
                "  lower double precision NOT NULL,\n" +
                "  upper double precision NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE integer_constraint_range (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  mapping_id integer REFERENCES mapping (id) NOT NULL,\n" +
                "  lower integer NOT NULL,\n" +
                "  upper integer NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE categorical_constraint_value (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  mapping_id integer REFERENCES mapping (id) NOT NULL,\n" +
                "  value varchar NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE boolean_constraint_value (\n" +
                "  id serial PRIMARY KEY,\n" +
                "  mapping_id integer REFERENCES mapping (id) NOT NULL,\n" +
                "  value boolean NOT NULL\n" +
                ");\n";
    }
}
