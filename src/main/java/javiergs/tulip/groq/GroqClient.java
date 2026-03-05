package javiergs.tulip.groq;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GroqClient {

  private final GroqConfig config;

  public GroqClient(GroqConfig config) {
    if (config == null)
      throw new IllegalArgumentException("GroqConfig cannot be null");

    if (config.getApiKey() == null || config.getApiKey().isBlank())
      throw new IllegalArgumentException("Missing Groq API key");

    this.config = config;
  }

  public String chat(String userPrompt) {
    return chat(List.of(GroqMessage.user(userPrompt)), 0.2, 800);
  }

  public String chat(List<GroqMessage> messages, double temperature, int maxTokens) {

    try {
      String endpoint = config.getBaseUrl() + "/chat/completions";

      JSONObject body = new JSONObject();
      body.put("model", config.getModel());
      body.put("temperature", temperature);
      body.put("max_tokens", maxTokens);

      JSONArray msgs = new JSONArray();
      for (GroqMessage m : messages) {
        msgs.put(new JSONObject()
            .put("role", m.getRole())
            .put("content", m.getContent()));
      }
      body.put("messages", msgs);

      HttpURLConnection con =
          (HttpURLConnection) new URL(endpoint).openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
      con.setDoOutput(true);

      byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
      con.getOutputStream().write(payload);

      int code = con.getResponseCode();

      if (code / 100 != 2) {
        String err = readAll(con.getErrorStream());
        throw new RuntimeException("Groq API error HTTP " + code + ": " + err);
      }

      String response = readAll(con.getInputStream());
      JSONObject json = new JSONObject(response);

      return json.getJSONArray("choices")
          .getJSONObject(0)
          .getJSONObject("message")
          .getString("content")
          .trim();

    } catch (Exception e) {
      throw new RuntimeException("Groq request failed: " + e.getMessage(), e);
    }
  }

  private static String readAll(java.io.InputStream in) throws Exception {

    if (in == null)
      return "";

    try (BufferedReader reader =
             new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

      StringBuilder sb = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null)
        sb.append(line);

      return sb.toString();
    }
  }
}