package javiergs.tulip.taiga;

public class TaigaUser {

  private final long id;
  private final String username;
  private final String fullName;
  private final String email;

  public TaigaUser(long id, String username, String fullName, String email) {
    this.id = id;
    this.username = username;
    this.fullName = fullName;
    this.email = email;
  }

  public long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public String toString() {
    return "TaigaUser{" +
        "id=" + id +
        ", username='" + username + '\'' +
        ", fullName='" + fullName + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
