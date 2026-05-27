package javiergs.tulip;

import javiergs.tulip.taiga.*;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Tag("integration")
class TaigaClientIntegrationTest {

  private static TaigaClient taiga;

  @BeforeAll
  static void setup() throws Exception {
    Properties cfg = loadProperties("config.properties");

    String host = require(cfg, "taiga.host");
    String username = require(cfg, "taiga.username");
    String password = require(cfg, "taiga.password");

    taiga = new TaigaClient(host);
    taiga.login(username, password);
  }

  @Test
  @DisplayName("getMyProjects should return accessible Taiga projects")
  void getMyProjects_returnsProjects() throws Exception {
    List<TaigaProject> projects = taiga.getMyProjects();

    Assertions.assertNotNull(projects);
    Assertions.assertFalse(projects.isEmpty(), "Expected at least one accessible Taiga project");
  }

  @Test
  @DisplayName("projects should load sprints, stories, and tasks without errors")
  void projectData_loadsWithoutErrors() throws Exception {
    List<TaigaProject> projects = taiga.getMyProjects();

    Assertions.assertNotNull(projects);
    Assertions.assertFalse(projects.isEmpty(), "Expected at least one project");

    TaigaProject project = projects.get(0);

    List<TaigaSprint> sprints = taiga.getSprints(project.getId());
    List<TaigaUserStory> stories = taiga.getStories(project.getId());
    List<TaigaTask> tasks = taiga.getTasks(project.getId());

    Assertions.assertNotNull(sprints);
    Assertions.assertNotNull(stories);
    Assertions.assertNotNull(tasks);
  }

  @Test
  @DisplayName("tasks should expose parent user story id when provided by Taiga")
  void tasks_exposeUserStoryId() throws Exception {
    List<TaigaProject> projects = taiga.getMyProjects();
    Assertions.assertFalse(projects.isEmpty(), "Expected at least one project");

    TaigaProject project = projects.get(0);
    List<TaigaTask> tasks = taiga.getTasks(project.getId());

    Assertions.assertNotNull(tasks);

    for (TaigaTask task : tasks) {
      Assertions.assertNotNull(task);
      // This can be null if Taiga returns a task not attached to a story.
      task.getUserStoryId();
    }
  }

  @Test
  @DisplayName("user stories should expose milestone id")
  void stories_exposeMilestoneId() throws Exception {
    List<TaigaProject> projects = taiga.getMyProjects();
    Assertions.assertFalse(projects.isEmpty(), "Expected at least one project");

    TaigaProject project = projects.get(0);
    List<TaigaUserStory> stories = taiga.getStories(project.getId());

    Assertions.assertNotNull(stories);

    for (TaigaUserStory story : stories) {
      Assertions.assertNotNull(story);
      // Can be null if the story is not assigned to a sprint.
      story.getMilestoneId();
    }
  }

  private static Properties loadProperties(String resourceName) throws Exception {
    Properties p = new Properties();
    try (InputStream in = TaigaClientIntegrationTest.class
        .getClassLoader()
        .getResourceAsStream(resourceName)) {
      if (in == null) {
        throw new RuntimeException(resourceName + " not found in src/test/resources");
      }
      p.load(in);
    }
    return p;
  }

  private static String require(Properties p, String key) {
    String v = p.getProperty(key);
    if (v == null || v.isBlank()) {
      throw new IllegalArgumentException("Missing property: " + key);
    }
    return v.trim();
  }

  @Test
  @DisplayName("stories by sprint should match milestone id when stories exist")
  void storiesBySprint_matchMilestoneId_whenStoriesExist() throws Exception {
    List<TaigaProject> projects = taiga.getMyProjects();

    boolean checkedAtLeastOneStory = false;

    for (TaigaProject project : projects) {
      for (TaigaSprint sprint : taiga.getSprints(project.getId())) {
        List<TaigaUserStory> stories = taiga.getStoriesBySprint(sprint.getId());

        for (TaigaUserStory story : stories) {
          checkedAtLeastOneStory = true;
          Assertions.assertEquals(sprint.getId(), story.getMilestoneId());
        }
      }
    }

    Assertions.assertTrue(checkedAtLeastOneStory,
        "Expected at least one story assigned to a sprint in available projects");
  }

  @Test
  @DisplayName("tasks with userStoryId should reference an existing story when provided")
  void tasksWithUserStoryId_referenceExistingStory_whenProvided() throws Exception {
    List<TaigaProject> projects = taiga.getMyProjects();

    boolean checkedAtLeastOneLinkedTask = false;

    for (TaigaProject project : projects) {
      List<Long> storyIds = taiga.getStories(project.getId()).stream()
          .map(TaigaUserStory::getId)
          .toList();

      for (TaigaTask task : taiga.getTasks(project.getId())) {
        Long userStoryId = task.getUserStoryId();

        if (userStoryId != null) {
          checkedAtLeastOneLinkedTask = true;
          Assertions.assertTrue(
              storyIds.contains(userStoryId),
              "Task userStoryId should reference an existing story: " + userStoryId
          );
        }
      }
    }
    Assertions.assertTrue(checkedAtLeastOneLinkedTask,
        "Expected at least one task linked to a user story in available projects");
  }

}