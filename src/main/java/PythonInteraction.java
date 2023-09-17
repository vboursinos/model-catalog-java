import com.fasterxml.jackson.databind.ObjectMapper;
import model.Models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class PythonInteraction {
    public static void main(String[] args) throws IOException, InterruptedException {
        String script = "/home/vasilis/PycharmProjects/model-catalog-py/src/main.py";
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
        System.out.println("Conversion script is running");
        p = pb.start();
        if (p.waitFor() != 0) {
            System.err.println("An error ocurred executing the conversion script.");
            throw new IOException(p.getErrorStream().toString());
        }
        System.out.println("Script executed successfully");
        CreateSqlScript createSqlScript = new CreateSqlScript();
        createSqlScript.createFiles();
    }
}
