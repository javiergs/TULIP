package example;

import javiergs.tulip.github.GitHubHandler;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {

        Properties p = new Properties();

        try (InputStream in =
                 Main.class.getClassLoader()
                     .getResourceAsStream("tulip.properties")) {

            if (in != null) {
                p.load(in);
            }
        }
        String token = p.getProperty("GITHUB_TOKEN");
        GitHubHandler gh = new GitHubHandler(token);

        List<String> files =
            gh.listFilesRecursive(
                "https://github.com/javiergs/ADASIM"
            );

        for (String file : files) {
            System.out.println(file);
        }
    }
}
