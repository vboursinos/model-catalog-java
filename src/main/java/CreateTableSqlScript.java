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
}
