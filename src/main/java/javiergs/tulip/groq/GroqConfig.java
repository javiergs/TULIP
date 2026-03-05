package javiergs.tulip.groq;

public class GroqConfig {
  private final String apiKey;
  private final String baseUrl;
  private final String model;

  public GroqConfig(String apiKey, String baseUrl, String model) {
    this.apiKey = apiKey;
    this.baseUrl = (baseUrl == null || baseUrl.isBlank())
        ? "https://api.groq.com/openai/v1"
        : stripTrailingSlash(baseUrl);
    this.model = (model == null || model.isBlank())
        ? "llama-3.3-70b-versatile"
        : model;
  }

  public String getApiKey() { return apiKey; }
  public String getBaseUrl() { return baseUrl; }
  public String getModel() { return model; }

  private static String stripTrailingSlash(String s) {
    return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
  }
}
