package example;

import javiergs.tulip.taiga.*;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {

        Properties p = new Properties();

        try (InputStream in =
                 Main.class.getClassLoader()
                     .getResourceAsStream("tulip.properties")) {

            p.load(in);
        }

        TaigaClient taiga =
            new TaigaClient(
                p.getProperty("taiga.host")
            );

        taiga.login(
            p.getProperty("taiga.username"),
            p.getProperty("taiga.password")
        );

        List<TaigaProject> projects =
            taiga.getMyProjects();

        for (TaigaProject project : projects) {
            System.out.println(project.getName());
        }
    }
}
