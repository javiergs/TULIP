package javiergs.tulip.taiga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

  /**
   * Login using Taiga username and password.
   */
  public void login(String username, String password) throws Exception {

    String body = """
        {
            "type":"normal",
            "username":"%s",
            "password":"%s"
        }
        """.formatted(jsonEscape(username), jsonEscape(password));

    JsonNode json = postJson("/api/v1/auth", body);

    this.authToken = requireText(json,
        "auth_token",
        "Login failed. No auth_token returned");
  }

  // ---------------------------
  // PUBLIC API
  // ---------------------------

  public List<TaigaProject> getMyProjects() throws Exception {
    long myId = getMyUserId();
    JsonNode arr = getJson("/api/v1/projects?member=" + myId);

    List<TaigaProject> out = new ArrayList<>();
    for (JsonNode p : arr) {
      out.add(new TaigaProject(
          p.path("id").asLong(),
          p.path("name").asText(""),
          p.path("description").asText("")
      ));
    }
    return out;
  }

  public List<TaigaUserStory> getStories(long projectId) throws Exception {
    JsonNode arr = getJson("/api/v1/userstories?project=" + projectId);

    List<TaigaUserStory> out = new ArrayList<>();
    for (JsonNode s : arr) {
      out.add(new TaigaUserStory(
          s.path("id").asLong(),
          s.path("ref").asInt(),
          s.path("subject").asText(""),
          s.path("status").asInt()
              , s.path("milestone").isNull() ? null : s.path("milestone").asLong()
      ));
    }
    return out;
  }

  public List<TaigaTask> getTasks(long projectId) throws Exception {
    JsonNode arr = getJson("/api/v1/tasks?project=" + projectId);

    List<TaigaTask> out = new ArrayList<>();
    for (JsonNode t : arr) {
      Long assigned = t.path("assigned_to").isNull() ? null : t.path("assigned_to").asLong();
      out.add(new TaigaTask(
          t.path("id").asLong(),
          t.path("ref").asInt(),
          t.path("subject").asText(""),
          t.path("status").asInt(),
          assigned
      ));
    }
    return out;
  }

  /**
   * Taiga calls sprints "milestones".
   */
  public List<TaigaSprint> getSprints(long projectId) throws Exception {
    JsonNode arr = getJson("/api/v1/milestones?project=" + projectId);

    List<TaigaSprint> out = new ArrayList<>();
    for (JsonNode m : arr) {
      out.add(new TaigaSprint(
          m.path("id").asLong(),
          m.path("name").asText(""),
          m.path("estimated_start").asText(""),
          m.path("estimated_finish").asText("")
      ));
    }
    return out;
  }

  public List<TaigaUserStory> getStoriesBySprint(long sprintId) throws Exception {

    JsonNode arr = getJson("/api/v1/userstories?milestone=" + sprintId);

    List<TaigaUserStory> out = new ArrayList<>();
    for (JsonNode s : arr) {
      out.add(new TaigaUserStory(
          s.path("id").asLong(),
          s.path("ref").asInt(),
          s.path("subject").asText(""),
          s.path("status").asInt(),
          s.path("milestone").isNull() ? null : s.path("milestone").asLong()
      ));
    }
    return out;
  }


  // ---------------------------
  // INTERNALS
  // ---------------------------

  private long getMyUserId() throws Exception {
    JsonNode me = getJson("/api/v1/users/me");
    return me.path("id").asLong();
  }

  private JsonNode getJson(String path) throws Exception {
    requireLoggedIn();

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(host + path))
        .header("Authorization", "Bearer " + authToken)
        .header("Accept", "application/json")
        .GET()
        .build();

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    ensure2xx(res, "GET " + path);
    return mapper.readTree(res.body());
  }

  private JsonNode postJson(String path, String jsonBody) throws Exception {
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(host + path))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
        .build();

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    ensure2xx(res, "POST " + path);
    return mapper.readTree(res.body());
  }

  private void requireLoggedIn() {
    if (authToken == null || authToken.isBlank()) {
      throw new IllegalStateException("Not logged in. Call loginWithGithubCode(...) first.");
    }
  }

  private static void ensure2xx(HttpResponse<String> res, String label) {
    if (res.statusCode() / 100 != 2) {
      throw new RuntimeException(label + " failed HTTP " + res.statusCode() + "\n" + res.body());
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
    return (s != null && s.endsWith("/")) ? s.substring(0, s.length() - 1) : s;
  }

  private static String jsonEscape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}