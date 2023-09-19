import utils.FileUtils;

import java.nio.file.Paths;

public class CreateSqlScript {
    private static final String SQL_DIR_PATH = "sql_scripts";
    private CreateInsertScripts createInsertScripts;
    private CreateTableSqlScript createTableSqlScript;
    public CreateSqlScript() {
        createInsertScripts = new CreateInsertScripts();
        createTableSqlScript = new CreateTableSqlScript();
        FileUtils.createDirectory(Paths.get(SQL_DIR_PATH));
    }

    public void createFiles() {
        createTableSqlScript.createTablesScript();
        createInsertScripts.insertDataScripts();
    }
}
