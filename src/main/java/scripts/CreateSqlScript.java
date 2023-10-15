package scripts;

import java.nio.file.Paths;
import scripts.create_tables.CreateTableSqlScript;
import scripts.inserts.InsertDynamicTables;
import scripts.inserts.InsertStaticTables;
import utils.FileUtils;

public class CreateSqlScript {
  private static final String SQL_DIR_PATH = "sql_scripts";
  private InsertDynamicTables insertDynamicTables;
  private CreateTableSqlScript createTableSqlScript;

  private InsertStaticTables insertStaticTables;

  public CreateSqlScript() {
    insertDynamicTables = new InsertDynamicTables();
    createTableSqlScript = new CreateTableSqlScript();
    insertStaticTables = new InsertStaticTables();
    FileUtils.createDirectory(Paths.get(SQL_DIR_PATH));
  }

  public void createFiles() {
    createTableSqlScript.createTablesScript();
    insertStaticTables.insertDataScripts();
    insertDynamicTables.insertDataScripts();
  }
}
