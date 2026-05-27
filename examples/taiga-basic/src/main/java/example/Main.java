package example;

import javiergs.tulip.taiga.*;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {

        Properties cfg = loadProperties("tulip.properties");

        TaigaClient taiga = new TaigaClient(
                require(cfg, "taiga.host")
        );

        taiga.login(
                require(cfg, "taiga.username"),
                require(cfg, "taiga.password")
        );

        List<TaigaProject> projects = taiga.getMyProjects();

        if (projects.isEmpty()) {
            System.out.println("No accessible Taiga projects found.");
            return;
        }

        TaigaProject project = projects.get(0);

        System.out.println("Using project: " + project.getName());

        String suffix = String.valueOf(System.currentTimeMillis());

        TaigaSprint sprint = null;
        TaigaUserStory story = null;
        TaigaTask task = null;

        try {
            sprint = taiga.createSprint(
                    project.getId(),
                    "Example Sprint " + suffix,
                    "2026-06-01",
                    "2026-06-14"
            );

            System.out.println("Created sprint: " + sprint.getName());

            story = taiga.createUserStory(
                    project.getId(),
                    "Example Story " + suffix,
                    "Created from the TULIP Taiga example.",
                    sprint.getId()
            );

            System.out.println("Created story: " + story.getSubject());

            task = taiga.createTask(
                    project.getId(),
                    story.getId(),
                    "Example Task " + suffix,
                    null
            );

            System.out.println("Created task: " + task.getSubject());

            TaigaTask fetchedTask = taiga.getTask(task.getId());

            System.out.println(
                    "Fetched task: " + fetchedTask.getId()
                            + " / story id: " + fetchedTask.getUserStoryId()
            );

            List<TaigaStatus> statuses =
                    taiga.getTaskStatuses(project.getId());

            if (statuses.size() >= 2) {
                long currentStatusId = task.getStatusId();

                long newStatusId = statuses.stream()
                        .map(TaigaStatus::getId)
                        .filter(id -> id != currentStatusId)
                        .findFirst()
                        .orElseThrow();

                TaigaTask movedTask =
                        taiga.moveTaskToStatus(task.getId(), newStatusId);

                System.out.println(
                        "Moved task to status id: "
                                + movedTask.getStatusId()
                );
            } else {
                System.out.println(
                        "Project does not have enough task statuses to test movement."
                );
            }

        } finally {
            if (task != null) {
                taiga.deleteTask(task.getId());
                System.out.println("Deleted task.");
            }

            if (story != null) {
                taiga.deleteUserStory(story.getId());
                System.out.println("Deleted story.");
            }

            if (sprint != null) {
                taiga.deleteSprint(sprint.getId());
                System.out.println("Deleted sprint.");
            }
        }
    }

    private static Properties loadProperties(String resourceName) throws Exception {
        Properties p = new Properties();

        try (InputStream in =
                     Main.class.getClassLoader()
                             .getResourceAsStream(resourceName)) {

            if (in == null) {
                throw new RuntimeException(
                        resourceName + " not found in src/main/resources"
                );
            }

            p.load(in);
        }

        return p;
    }

    private static String require(Properties p, String key) {
        String value = p.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing property: " + key);
        }

        return value.trim();
    }
}
