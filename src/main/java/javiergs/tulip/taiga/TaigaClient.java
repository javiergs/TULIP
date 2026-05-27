package javiergs.tulip.taiga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaigaClient {

  private final String host;
  private final HttpClient http;
  private final ObjectMapper mapper;

  private String authToken;


  public TaigaClient(String host) {
    this.host = stripTrailingSlash(host);
    this.http = HttpClient.newHttpClient();
    this.mapper = new ObjectMapper();
  }

  public void login(String username, String password) throws Exception {
    String body = """
        {
            "type":"normal",
            "username":"%s",
            "password":"%s"
        }
        """.formatted(jsonEscape(username), jsonEscape(password));

    JsonNode json = postJsonWithoutAuth("/api/v1/auth", body);

    this.authToken = requireText(json,
        "auth_token",
        "Login failed. No auth_token returned");
  }

  public List<TaigaProject> getMyProjects() throws Exception {
    long myId = getMyUserId();
    //JsonNode arr = getJson("/api/v1/projects?member=" + myId);
    JsonNode arr = getJsonArrayAllPages("/api/v1/projects?member=" + myId);
    List<TaigaProject> out = new ArrayList<>();
    for (JsonNode p : arr) {
      out.add(toProject(p));
    }
    return out;
  }

  public TaigaProject getProject(long projectId) throws IOException, InterruptedException {
    return toProject(getJson("/api/v1/projects/" + projectId));
  }

  public List<TaigaSprint> getSprints(long projectId) throws Exception {
    //JsonNode arr = getJson("/api/v1/milestones?project=" + projectId);
    JsonNode arr = getJsonArrayAllPages("/api/v1/milestones?project=" + projectId);

    List<TaigaSprint> out = new ArrayList<>();
    for (JsonNode m : arr) {
      out.add(toSprint(m));
    }
    return out;
  }

  public TaigaSprint getSprint(long sprintId) throws IOException, InterruptedException {
    return toSprint(getJson("/api/v1/milestones/" + sprintId));

  }

  public List<TaigaUserStory> getStories(long projectId) throws Exception {
    //JsonNode arr = getJson("/api/v1/userstories?project=" + projectId);
    JsonNode arr = getJsonArrayAllPages("/api/v1/userstories?project=" + projectId);

    List<TaigaUserStory> out = new ArrayList<>();
    for (JsonNode s : arr) {
      out.add(toUserStory(s));
    }
    return out;
  }

  public TaigaUserStory getStory(long storyId) throws IOException, InterruptedException {
    return toUserStory(getJson("/api/v1/userstories/" + storyId));
  }

  public List<TaigaUserStory> getStoriesBySprint(long sprintId) throws Exception {
    //JsonNode arr = getJson("/api/v1/userstories?milestone=" + sprintId);
    JsonNode arr = getJsonArrayAllPages("/api/v1/userstories?milestone=" + sprintId);

    List<TaigaUserStory> out = new ArrayList<>();
    for (JsonNode s : arr) {
      out.add(toUserStory(s));
    }
    return out;
  }

  public List<TaigaTask> getTasks(long projectId) throws Exception {
    //JsonNode arr = getJson("/api/v1/tasks?project=" + projectId);
    JsonNode arr = getJsonArrayAllPages("/api/v1/tasks?project=" + projectId);
    List<TaigaTask> out = new ArrayList<>();
    for (JsonNode t : arr) {
      out.add(toTask(t));
    }
    return out;
  }

  public TaigaTask getTask(long taskId) throws IOException, InterruptedException {
    return toTask(getJson("/api/v1/tasks/" + taskId));
  }

  public TaigaUser getUserById(long userId) throws Exception {
    JsonNode u = getJson("/api/v1/users/" + userId);

    String fullName =
        !u.path("full_name_display").asText("").isBlank()
            ? u.path("full_name_display").asText("")
            : u.path("full_name").asText("");

    return new TaigaUser(
        u.path("id").asLong(),
        u.path("username").asText(""),
        fullName,
        u.path("email").asText("")
    );
  }

  public List<TaigaStatus> getTaskStatuses(long projectId) throws Exception {
    //JsonNode arr = getJson("/api/v1/task-statuses?project=" + projectId);
    JsonNode arr = getJsonArrayAllPages("/api/v1/task-statuses?project=" + projectId);
    List<TaigaStatus> out = new ArrayList<>();
    for (JsonNode s : arr) {
      out.add(new TaigaStatus(
          s.path("id").asLong(),
          s.path("name").asText(""),
          s.path("slug").asText(""),
          s.path("color").asText(""),
          s.path("order").asInt()
      ));
    }
    return out;
  }

  public Map<Long, String> getTaskStatusMap(long projectId) throws Exception {
    Map<Long, String> map = new HashMap<>();
    for (TaigaStatus s : getTaskStatuses(projectId)) {
      map.put(s.getId(), s.getName());
    }
    return map;
  }

  public TaigaProject createProject(String name, String description)
      throws IOException, InterruptedException {
    ObjectNode body = mapper.createObjectNode();
    body.put("name", name);
    body.put("description", description == null ? "" : description);

    return toProject(postJson("/api/v1/projects", body));
  }

  public TaigaSprint createSprint(long projectId, String name,
                                  String estimatedStart,
                                  String estimatedFinish)
      throws IOException, InterruptedException {
    ObjectNode body = mapper.createObjectNode();
    body.put("project", projectId);
    body.put("name", name);
    body.put("estimated_start", estimatedStart);
    body.put("estimated_finish", estimatedFinish);

    return toSprint(postJson("/api/v1/milestones", body));
  }

  public TaigaUserStory createUserStory(long projectId, String subject,
                                        String description,
                                        Long milestoneId)
      throws IOException, InterruptedException {
    ObjectNode body = mapper.createObjectNode();
    body.put("project", projectId);
    body.put("subject", subject);
    body.put("description", description == null ? "" : description);

    if (milestoneId != null) {
      body.put("milestone", milestoneId);
    }

    return toUserStory(postJson("/api/v1/userstories", body));
  }

  public TaigaTask createTask(long projectId, Long userStoryId,
                              String subject,
                              Long assignedToUserId)
      throws IOException, InterruptedException {
    ObjectNode body = mapper.createObjectNode();
    body.put("project", projectId);
    body.put("subject", subject);

    if (userStoryId != null) {
      body.put("user_story", userStoryId);
    }

    if (assignedToUserId != null) {
      body.put("assigned_to", assignedToUserId);
    }

    return toTask(postJson("/api/v1/tasks", body));
  }

  public TaigaUserStory moveStoryToSprint(long storyId, Long sprintId)
      throws IOException, InterruptedException {
    ObjectNode body = mapper.createObjectNode();

    if (sprintId == null) {
      body.putNull("milestone");
    } else {
      body.put("milestone", sprintId);
    }

    return toUserStory(patchJson("/api/v1/userstories/" + storyId, body));
  }

  public TaigaTask assignTaskToStory(long taskId, Long userStoryId)
      throws IOException, InterruptedException {
    ObjectNode body = mapper.createObjectNode();

    if (userStoryId == null) {
      body.putNull("user_story");
    } else {
      body.put("user_story", userStoryId);
    }

    return toTask(patchJson("/api/v1/tasks/" + taskId, body));
  }

  private long getMyUserId() throws Exception {
    JsonNode me = getJson("/api/v1/users/me");
    return me.path("id").asLong();
  }

  private JsonNode getJson(String path) throws IOException, InterruptedException {
    requireLoggedIn();

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(host + path))
        .header("Authorization", "Bearer " + authToken)
        .header("Accept", "application/json")
        .GET()
        .build();

    HttpResponse<String> res =
        http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    ensure2xx(res, "GET " + path);
    return mapper.readTree(res.body());
  }

  private JsonNode postJsonWithoutAuth(String path, String jsonBody) throws Exception {
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(host + path))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
        .build();

    HttpResponse<String> res =
        http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    ensure2xx(res, "POST " + path);
    return mapper.readTree(res.body());
  }

  private JsonNode postJson(String path, JsonNode body)
      throws IOException, InterruptedException {
    requireLoggedIn();

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(host + path))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .header("Authorization", "Bearer " + authToken)
        .POST(HttpRequest.BodyPublishers.ofString(
            mapper.writeValueAsString(body), StandardCharsets.UTF_8))
        .build();

    HttpResponse<String> res =
        http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    ensure2xx(res, "POST " + path);
    return mapper.readTree(res.body());
  }

  private JsonNode patchJson(String path, JsonNode body)
      throws IOException, InterruptedException {
    requireLoggedIn();

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(host + path))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .header("Authorization", "Bearer " + authToken)
        .method("PATCH", HttpRequest.BodyPublishers.ofString(
            mapper.writeValueAsString(body), StandardCharsets.UTF_8))
        .build();

    HttpResponse<String> res =
        http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    ensure2xx(res, "PATCH " + path);
    return mapper.readTree(res.body());
  }

  private TaigaProject toProject(JsonNode p) {
    return new TaigaProject(
        p.path("id").asLong(),
        p.path("name").asText(""),
        p.path("description").asText("")
    );
  }

  private TaigaSprint toSprint(JsonNode s) {
    return new TaigaSprint(
        s.path("id").asLong(),
        s.path("name").asText(""),
        s.path("estimated_start").asText(""),
        s.path("estimated_finish").asText("")
    );
  }

  private TaigaUserStory toUserStory(JsonNode us) {
    Long milestoneId = us.path("milestone").isNull()
        ? null
        : us.path("milestone").asLong();

    return new TaigaUserStory(
        us.path("id").asLong(),
        us.path("ref").asInt(),
        us.path("subject").asText(""),
        us.path("status").asInt(),
        milestoneId,
        us.path("version").asLong());
  }

  private TaigaTask toTask(JsonNode t) {
    Long assignedToUserId = t.path("assigned_to").isNull()
        ? null
        : t.path("assigned_to").asLong();

    Long userStoryId = t.path("user_story").isNull()
        ? null
        : t.path("user_story").asLong();

    return new TaigaTask(
        t.path("id").asLong(),
        t.path("ref").asInt(),
        t.path("subject").asText(""),
        t.path("status").asInt(),
        assignedToUserId,
        userStoryId,
        t.path("version").asLong()
    );
  }

  private void requireLoggedIn() {
    if (authToken == null || authToken.isBlank()) {
      throw new IllegalStateException("Not logged in. Call login(...) first.");
    }
  }

  private static void ensure2xx(HttpResponse<String> res, String label) {
    if (res.statusCode() / 100 != 2) {
      throw new RuntimeException(
          label + " failed HTTP "
              + res.statusCode()
              + "\n"
              + res.body()
      );
    } else if (res.statusCode() == 429) {
      throw new RuntimeException(
          label + " failed because Taiga throttled the request.\n"
              + "HTTP 429 means too many API requests were sent in a short time.\n"
              + "Wait a few minutes or reduce repeated integration-test calls.\n"
              + "Server response:\n"
              + res.body()
      );
    }
  }

  private static String requireText(JsonNode obj, String field, String errorMessage) {
    JsonNode n = obj.get(field);
    if (n == null || n.isNull() || n.asText().isBlank()) {
      throw new RuntimeException(errorMessage + ":\n" + obj.toPrettyString());
    }
    return n.asText();
  }

  private static String stripTrailingSlash(String s) {
    return (s != null && s.endsWith("/"))
        ? s.substring(0, s.length() - 1)
        : s;
  }

  private static String jsonEscape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }


  // STAGE 3 for DELETE

  private void deleteJson(String path) throws IOException, InterruptedException {
    requireLoggedIn();
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(host + path))
        .header("Accept", "application/json")
        .header("Authorization", "Bearer " + authToken)
        .DELETE()
        .build();
    HttpResponse<String> res =
        http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    ensure2xx(res, "DELETE " + path);
  }

  public void deleteTask(long taskId) throws IOException, InterruptedException {
    deleteJson("/api/v1/tasks/" + taskId);
  }

  public void deleteUserStory(long storyId) throws IOException, InterruptedException {
    deleteJson("/api/v1/userstories/" + storyId);
  }

  public void deleteSprint(long sprintId) throws IOException, InterruptedException {
    deleteJson("/api/v1/milestones/" + sprintId);
  }

  public void deleteProject(long projectId) throws IOException, InterruptedException {
    deleteJson("/api/v1/projects/" + projectId);
  }

  // STAGE 4 PAGINATION and UPDATE

  private JsonNode getJsonArrayAllPages(String path)
      throws IOException, InterruptedException {

    requireLoggedIn();

    List<JsonNode> all = new ArrayList<>();
    int page = 1;

    while (true) {
      String separator = path.contains("?") ? "&" : "?";
      String pagedPath = path + separator + "page=" + page;

      HttpRequest req = HttpRequest.newBuilder()
          .uri(URI.create(host + pagedPath))
          .header("Authorization", "Bearer " + authToken)
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> res =
          http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

      if (res.statusCode() == 404) {
        break;
      }

      ensure2xx(res, "GET " + pagedPath);

      JsonNode arr = mapper.readTree(res.body());

      if (!arr.isArray() || arr.isEmpty()) {
        break;
      }

      for (JsonNode item : arr) {
        all.add(item);
      }

      page++;
    }

    return mapper.valueToTree(all);
  }

  public TaigaProject updateProject(long projectId, String name, String description)
      throws IOException, InterruptedException {
    ObjectNode body = mapper.createObjectNode();

    if (name != null) {
      body.put("name", name);
    }

    if (description != null) {
      body.put("description", description);
    }

    return toProject(patchJson("/api/v1/projects/" + projectId, body));
  }

  public TaigaSprint updateSprint(long sprintId, String name,
                                  String estimatedStart,
                                  String estimatedFinish)
      throws IOException, InterruptedException {
    ObjectNode body = mapper.createObjectNode();

    if (name != null) {
      body.put("name", name);
    }

    if (estimatedStart != null) {
      body.put("estimated_start", estimatedStart);
    }

    if (estimatedFinish != null) {
      body.put("estimated_finish", estimatedFinish);
    }

    return toSprint(patchJson("/api/v1/milestones/" + sprintId, body));
  }

  public TaigaUserStory updateUserStory(long storyId, String subject,
                                        String description,
                                        Long milestoneId)
      throws IOException, InterruptedException {

    TaigaUserStory current = getStory(storyId);

    ObjectNode body = mapper.createObjectNode();

    if (subject != null) {
      body.put("subject", subject);
    }

    if (description != null) {
      body.put("description", description);
    }

    if (milestoneId != null) {
      body.put("milestone", milestoneId);
    }

    body.put("version", current.getVersion());

    return toUserStory(patchJson("/api/v1/userstories/" + storyId, body));
  }

  public TaigaTask updateTask(long taskId, String subject,
                              Long assignedToUserId,
                              Long userStoryId)
      throws IOException, InterruptedException {

    TaigaTask current = getTask(taskId);

    ObjectNode body = mapper.createObjectNode();

    if (subject != null) {
      body.put("subject", subject);
    }

    if (assignedToUserId != null) {
      body.put("assigned_to", assignedToUserId);
    }

    if (userStoryId != null) {
      body.put("user_story", userStoryId);
    }

    body.put("version", current.getVersion());

    return toTask(patchJson("/api/v1/tasks/" + taskId, body));
  }

  public TaigaTask moveTaskToStatus(long taskId, long statusId)
      throws IOException, InterruptedException {

    TaigaTask current = getTask(taskId);

    ObjectNode body = mapper.createObjectNode();
    body.put("status", statusId);
    body.put("version", current.getVersion());

    return toTask(patchJson("/api/v1/tasks/" + taskId, body));
  }



}