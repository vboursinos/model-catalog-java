package configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesConfig {

  private static final String PROPERTIES_FILE_PATH = "src/main/resources/configuration.properties";

  public static Properties getProperties() {
    Properties properties = new Properties();
    try {
      properties.load(Files.newInputStream(Paths.get(PROPERTIES_FILE_PATH)));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return properties;
  }
}
