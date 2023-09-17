import java.io.IOException;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class PythonInteraction {
    private static final Logger logger = LogManager.getLogger(PythonInteraction.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        String script = "model-catalog-py/src/main.py";
        ProcessBuilder pb;
        Process p;
        pb =
                new ProcessBuilder(
                        "bash",
                        "-c",
                        String.format(
                                "python3 %s",
                                script))
                        .redirectErrorStream(true)
                        .inheritIO();
        pb.directory(Paths.get(".").toFile());
        logger.info("Python script is running");
        p = pb.start();
        if (p.waitFor() != 0) {
            System.err.println("An error ocurred executing the conversion script.");
            throw new IOException(p.getErrorStream().toString());
        }
        logger.info("Script executed successfully");
        CreateSqlScript createSqlScript = new CreateSqlScript();
        createSqlScript.createFiles();
    }
}
