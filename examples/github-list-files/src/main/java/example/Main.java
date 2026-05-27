package example;

import javiergs.tulip.github.GitHubHandler;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        GitHubHandler gh = new GitHubHandler();

        List<String> files =
            gh.listFilesRecursive(
                "https://github.com/javiergs/ADASIM"
            );

        for (String file : files) {
            System.out.println(file);
        }
    }
}
