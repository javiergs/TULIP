package javiergs.tulip.groq;

public class GroqMessage {
  private final String role;     // "system" | "user" | "assistant"
  private final String content;

  public GroqMessage(String role, String content) {
    this.role = role;
    this.content = content;
  }

  public String getRole() { return role; }
  public String getContent() { return content; }

  public static GroqMessage system(String text) { return new GroqMessage("system", text); }
  public static GroqMessage user(String text) { return new GroqMessage("user", text); }
}