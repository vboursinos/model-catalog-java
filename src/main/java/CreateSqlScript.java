import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;
import utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateSqlScript {

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
        String sqlScript =
                "-- Model Table\n" +
                        "CREATE TABLE Model (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    name TEXT,\n" +
                        "    mlTask TEXT\n" +
                        ");\n\n" +

                        "-- Metadata Table\n" +
                        "CREATE TABLE Metadata (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    model TEXT,\n" +
                        "    model_description TEXT,\n" +
                        "    display_name TEXT,\n" +
                        "    structure TEXT\n" +
                        ");\n\n" +

                        "-- Parameters Table\n" +
                        "CREATE TABLE Parameters (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    model_id INT REFERENCES Model(id),\n" +
                        "    metadata_id INT REFERENCES Metadata(id)\n" +
                        ");\n\n" +

                        "-- IncompatibleMetrics Table\n" +
                        "CREATE TABLE IncompatibleMetrics (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    metric TEXT,\n" +
                        "    model_id INT REFERENCES Model(id)\n" +
                        ");\n\n" +

                        "-- Groups Table\n" +
                        "CREATE TABLE Groups (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    group_item TEXT,\n" +
                        "    model_id INT REFERENCES Model(id)\n" +
                        ");\n\n" +

                        "-- InputParameter Table\n" +
                        "CREATE TABLE InputParameter (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    parameter_name TEXT,\n" +
                        "    parameter_type TEXT,\n" +
                        "    min_value INT,\n" +
                        "    max_value INT,\n" +
                        "    default_value TEXT,\n" +
                        "    label TEXT,\n" +
                        "    description TEXT,\n" +
                        "    enabled BOOLEAN,\n" +
                        "    has_constraint BOOLEAN,\n" +
                        "    constraint_information TEXT,\n" +
                        "    fixed_value BOOLEAN,\n" +
                        "    parameters_id INT REFERENCES Parameters(id)\n" +
                        ");\n\n" +

                        "-- InputParameterValues Table\n" +
                        "CREATE TABLE InputParameterValues (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    value TEXT,\n" +
                        "    input_parameter_id INT REFERENCES InputParameter(id)\n" +
                        ");\n\n" +

                        "-- ModelType Table\n" +
                        "CREATE TABLE ModelType (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    type TEXT,\n" +
                        "    metadata_id INT REFERENCES Metadata(id)\n" +
                        ");\n\n" +

                        "-- Advantage Table\n" +
                        "CREATE TABLE Advantage (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    advantage TEXT,\n" +
                        "    metadata_id INT REFERENCES Metadata(id)\n" +
                        ");\n\n" +

                        "-- Disadvantage Table\n" +
                        "CREATE TABLE Disadvantage (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    disadvantage TEXT,\n" +
                        "    metadata_id INT REFERENCES Metadata(id)\n" +
                        ");\n\n" +

                        "-- Prime Table\n" +
                        "CREATE TABLE Prime (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    prime TEXT,\n" +
                        "    metadata_id INT REFERENCES Metadata(id)\n" +
                        ");\n\n" +

                        "-- Support Table\n" +
                        "CREATE TABLE Support (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    probabilities BOOLEAN,\n" +
                        "    feature_importances BOOLEAN,\n" +
                        "    decision_tree BOOLEAN,\n" +
                        "    metadata_id INT REFERENCES Metadata(id)\n" +
                        ");\n";

        writeToFile(Paths.get(SQL_DIR_PATH,SETUP_SCRIPT_NAME).toString(), sqlScript);
    }

    public void writeToFile(String fileName, String content) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert writer != null;
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertDataScripts() {
        ObjectMapper mapper = new ObjectMapper();
        Path dirPath = Paths.get(JSON_DIR_PATH);
        Path sqlFilesPath = Paths.get(SQL_DIR_PATH);

        try {
            Files.newDirectoryStream(dirPath).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    Models models;
                    try {
                        models = mapper.readValue(filePath.toFile(), Models.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String mltask = models.getModels().get(0).getMlTask();
                    String sqlScript = buildInsertSQL(models);
                    String fileName = String.format("insert_models_%s.sql", mltask);
                    writeToFile(sqlFilesPath.resolve(fileName).toString(), sqlScript);

                    System.out.println(mltask + " sql file created successfully!");
                }
            });
        } catch (IOException e) {
            System.err.println("Error reading files from directory: " + e.getMessage());
        }

    }

    static String buildInsertSQL(Models models) {
        StringBuilder sb = new StringBuilder();
        for (Model model : models.getModels()) {
            String name = model.getName().replace("'", "''");  // escape single quote
            String mlTask = model.getMlTask().replace("'", "''");
            Metadata metadata = model.getParameters().getMetadata();
            String modelDescription = metadata.getModelDescription().replace("'", "''");

            sb.append("INSERT INTO Model(name, mlTask) VALUES ('").append(name).append("', '").append(mlTask).append("');\n");
            sb.append("INSERT INTO Metadata(model,model_description,display_name,structure) VALUES ('").append(metadata.getModel()).append("', '").append(modelDescription).append("', '").append(metadata.getDisplayName()).append("', '").append(metadata.getStructure()).append("');\n");
            sb.append("INSERT INTO Parameters(model_id, metadata_id) VALUES ((select id from model where name='").append(name).append("' and mlTask='").append(mlTask).append("'),(select id from metadata where model='").append(metadata.getModel()).append("'));\n");
            for (String modelType : metadata.getModelType()) {
                sb.append("INSERT INTO ModelType(type,metadata_id) values ('").append(modelType).append("', (select id from metadata where model='").append(metadata.getModel()).append("'));\n");
            }
            for (String advantage : metadata.getAdvantages()) {
                advantage = advantage.replace("'", "''");
                sb.append("INSERT INTO Advantage(advantage,metadata_id) values ('").append(advantage).append("', (select id from metadata where model='").append(metadata.getModel()).append("'));\n");
            }
            for (String disadvantage : metadata.getDisadvantages()) {
                disadvantage = disadvantage.replace("'", "''");
                sb.append("INSERT INTO Disadvantage(disadvantage,metadata_id) values ('").append(disadvantage).append("', (select id from metadata where model='").append(metadata.getModel()).append("'));\n");
            }
            for (String prime : metadata.getPrime()) {
                prime = prime.replace("'", "''");
                sb.append("INSERT INTO Prime(prime,metadata_id) values ('").append(prime).append("', (select id from metadata where model='").append(metadata.getModel()).append("'));\n");
            }
            Support support = metadata.getSupports();
            sb.append("INSERT INTO Support(probabilities,feature_importances,decision_tree,metadata_id) values (").append(support.getProbabilities()).append(",").append(support.getFeatureImportances()).append(",").append(support.getDecisionTree()).append(", (select id from metadata where model='").append(metadata.getModel()).append("'));\n");

            for (String incompatibleMetric : model.getIncompatibleMetrics()) {
                incompatibleMetric = incompatibleMetric.replace("'", "''");
                sb.append("INSERT INTO IncompatibleMetrics(metric,model_id) VALUES ('").append(incompatibleMetric).append("',(select id from model where name='").append(name).append("' and mlTask='").append(mlTask).append("'));\n");
            }
            for (String group : model.getGroups()) {
                group = group.replace("'", "''");
                sb.append("INSERT INTO Groups(group_item,model_id) VALUES ('").append(group).append("',(select id from model where name='").append(name).append("' and mlTask='").append(mlTask).append("'));\n");
            }
            for (InputParameter inputParameter : model.getParameters().getInputParameters()) {
                String description = inputParameter.getDescription().replace("'", "''");
                sb.append("INSERT INTO InputParameter(parameter_name,parameter_type,min_value,max_value,default_value,label,description,enabled,has_constraint,constraint_information,fixed_value,parameters_id) VALUES ('").append(inputParameter.getParameterName()).append("','").append(inputParameter.getParameterType()).append("','").append(inputParameter.getMinValue()).append("','").append(inputParameter.getMaxValue()).append("','").append(inputParameter.getDefaultValue()).append("','").append(inputParameter.getLabel()).append("','").append(description).append("',").append(inputParameter.isEnabled()).append(",").append(inputParameter.isConstraint()).append(",'").append(inputParameter.getConstraintInformation()).append("','").append(inputParameter.isFixedValue()).append("',(select id from parameters where model_id=(select id from model where name='").append(name).append("' and mlTask='").append(mlTask).append("')));\n");
                for (String value : inputParameter.getValues()) {
                    value = value.replace("'", "''");
                    sb.append("INSERT INTO InputParameterValues(value,input_parameter_id) VALUES ('").append(value).append("',(select id from InputParameter where parameter_name='").append(inputParameter.getParameterName()).append("' and parameters_id=(select id from parameters where model_id=(select id from model where name='").append(name).append("' and mlTask='").append(mlTask).append("'))));\n");
                }
            }
        }

        return sb.toString();
    }
}
