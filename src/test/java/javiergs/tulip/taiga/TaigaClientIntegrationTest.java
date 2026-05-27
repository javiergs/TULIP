package javiergs.tulip.taiga;

import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Tag("integration")
class TaigaClientIntegrationTest {

  private static TaigaClient taiga;
  private static List<TaigaProject> projects;

  @BeforeAll
  static void setup() throws Exception {
    Properties cfg = loadProperties("config.properties");

    String host = require(cfg, "taiga.host");
    String username = require(cfg, "taiga.username");
    String password = require(cfg, "taiga.password");

    taiga = new TaigaClient(host);
    taiga.login(username, password);
    projects = taiga.getMyProjects();
    Assertions.assertNotNull(projects);
    Assertions.assertFalse(projects.isEmpty(), "Expected at least one accessible Taiga project");
  }

  @Test
  @DisplayName("getMyProjects should return accessible Taiga projects")
  void getMyProjects_returnsProjects() throws Exception {
   //  List<TaigaProject> projects = taiga.getMyProjects();

    Assertions.assertNotNull(projects);
    Assertions.assertFalse(projects.isEmpty(), "Expected at least one accessible Taiga project");
  }

  @Disabled("Heavy API traversal causes Taiga throttling")
  @Test
  @DisplayName("projects should load sprints, stories, and tasks without errors")
  void projectData_loadsWithoutErrors() throws Exception {
   // List<TaigaProject> projects = taiga.getMyProjects();

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

  @Disabled("Heavy API traversal causes Taiga throttling")
  @Test
  @DisplayName("tasks should expose parent user story id when provided by Taiga")
  void tasks_exposeUserStoryId() throws Exception {
    //List<TaigaProject> projects = taiga.getMyProjects();
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
    // List<TaigaProject> projects = taiga.getMyProjects();
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

  @Disabled("Heavy API traversal causes Taiga throttling")
  @Test
  @DisplayName("stories by sprint should match milestone id when stories exist")
  void storiesBySprint_matchMilestoneId_whenStoriesExist() throws Exception {
    // List<TaigaProject> projects = taiga.getMyProjects();

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
  @DisplayName("tasks with userStoryId should reference a retrievable user story when provided")
  void tasksWithUserStoryId_referenceRetrievableStory_whenProvided() throws Exception {
    for (TaigaProject project : projects) {
      for (TaigaTask task : taiga.getTasks(project.getId())) {
        Long userStoryId = task.getUserStoryId();

        if (userStoryId != null) {
          TaigaUserStory story = taiga.getStory(userStoryId);

          Assertions.assertNotNull(story);
          Assertions.assertEquals(userStoryId.longValue(), story.getId());
          return;
        }
      }
    }

    Assertions.fail("Expected at least one task linked to a user story in available projects");
  }

  @Test
  @DisplayName("should create sprint, story, and task in existing project")
  void createSprintStoryAndTask_roundTrip() throws Exception {

    // List<TaigaProject> projects = taiga.getMyProjects();
    Assertions.assertFalse(projects.isEmpty(), "Expected at least one project");

    TaigaProject project = projects.get(0);

    String suffix = String.valueOf(System.currentTimeMillis());

    TaigaSprint sprint = null;
    TaigaUserStory story = null;
    TaigaTask task = null;

    try {

      sprint = taiga.createSprint(
          project.getId(),
          "TULIP Test Sprint " + suffix,
          "2026-06-01",
          "2026-06-14"
      );

      Assertions.assertNotNull(sprint);
      Assertions.assertTrue(sprint.getId() > 0);
      Assertions.assertEquals(
          "TULIP Test Sprint " + suffix,
          sprint.getName()
      );

      story = taiga.createUserStory(
          project.getId(),
          "TULIP Test Story " + suffix,
          "Created by TULIP integration test.",
          sprint.getId()
      );

      Assertions.assertNotNull(story);
      Assertions.assertTrue(story.getId() > 0);
      Assertions.assertEquals(
          sprint.getId(),
          story.getMilestoneId()
      );

      task = taiga.createTask(
          project.getId(),
          story.getId(),
          "TULIP Test Task " + suffix,
          null
      );

      Assertions.assertNotNull(task);
      Assertions.assertTrue(task.getId() > 0);
      Assertions.assertEquals(
          story.getId(),
          task.getUserStoryId()
      );

      TaigaTask fetchedTask = taiga.getTask(task.getId());

      Assertions.assertEquals(
          task.getId(),
          fetchedTask.getId()
      );

      Assertions.assertEquals(
          story.getId(),
          fetchedTask.getUserStoryId()
      );

    } finally {

      if (task != null) {
        taiga.deleteTask(task.getId());
      }

      if (story != null) {
        taiga.deleteUserStory(story.getId());
      }

      if (sprint != null) {
        taiga.deleteSprint(sprint.getId());
      }
    }
  }

  @Test
  @DisplayName("should update created sprint, story, and task")
  void updateCreatedSprintStoryAndTask_roundTrip() throws Exception {
    //TaigaProject project = taiga.getMyProjects().get(0);
    TaigaProject project = projects.get(0);
    String suffix = String.valueOf(System.currentTimeMillis());

    TaigaSprint sprint = null;
    TaigaUserStory story = null;
    TaigaTask task = null;

    try {
      sprint = taiga.createSprint(project.getId(), "Sprint " + suffix,
          "2026-06-01", "2026-06-14");

      story = taiga.createUserStory(project.getId(), "Story " + suffix,
          "Created for update test.", sprint.getId());

      task = taiga.createTask(project.getId(), story.getId(),
          "Task " + suffix, null);

      TaigaSprint updatedSprint = taiga.updateSprint(
          sprint.getId(), "Updated Sprint " + suffix, null, null);

      TaigaUserStory updatedStory = taiga.updateUserStory(
          story.getId(), "Updated Story " + suffix, null, null);

      TaigaTask updatedTask = taiga.updateTask(
          task.getId(), "Updated Task " + suffix, null, null);

      Assertions.assertEquals("Updated Sprint " + suffix, updatedSprint.getName());
      Assertions.assertEquals("Updated Story " + suffix, updatedStory.getSubject());
      Assertions.assertEquals("Updated Task " + suffix, updatedTask.getSubject());

    } finally {
      if (task != null) taiga.deleteTask(task.getId());
      if (story != null) taiga.deleteUserStory(story.getId());
      if (sprint != null) taiga.deleteSprint(sprint.getId());
    }
  }

  @Test
  @DisplayName("should move created task to another status when possible")
  void moveCreatedTaskToStatus_roundTrip() throws Exception {
    //TaigaProject project = taiga.getMyProjects().get(0);
    TaigaProject project = projects.get(0);
    String suffix = String.valueOf(System.currentTimeMillis());
    TaigaSprint sprint = null;
    TaigaUserStory story = null;
    TaigaTask task = null;

    try {
      sprint = taiga.createSprint(
          project.getId(),
          "Status Test Sprint " + suffix,
          "2026-06-01",
          "2026-06-14"
      );

      story = taiga.createUserStory(
          project.getId(),
          "Status Test Story " + suffix,
          "Created for status movement test.",
          sprint.getId()
      );

      task = taiga.createTask(
          project.getId(),
          story.getId(),
          "Status Test Task " + suffix,
          null
      );

      List<TaigaStatus> statuses = taiga.getTaskStatuses(project.getId());

      Assertions.assertNotNull(statuses);
      Assumptions.assumeTrue(statuses.size() >= 2,
          "Need at least two task statuses to test movement");

      long currentStatusId = task.getStatusId();

      long newStatusId = statuses.stream()
          .map(TaigaStatus::getId)
          .filter(id -> id != currentStatusId)
          .findFirst()
          .orElseThrow();

      TaigaTask movedTask = taiga.moveTaskToStatus(task.getId(), newStatusId);

      Assertions.assertNotNull(movedTask);
      Assertions.assertEquals(task.getId(), movedTask.getId());
      Assertions.assertEquals(newStatusId, movedTask.getStatusId());
      Assertions.assertNotEquals(currentStatusId, movedTask.getStatusId());

      TaigaTask reloadedTask = taiga.getTask(task.getId());

      Assertions.assertNotNull(reloadedTask);
      Assertions.assertEquals(newStatusId, reloadedTask.getStatusId());

    } finally {
      if (task != null) taiga.deleteTask(task.getId());
      if (story != null) taiga.deleteUserStory(story.getId());
      if (sprint != null) taiga.deleteSprint(sprint.getId());
    }
  }

}