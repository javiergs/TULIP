package javiergs.tulip.taiga;

public class TaigaProject {
  private final long id;
  private final String name;
  private final String description;

  public TaigaProject(long id, String name, String description) {
    this.id = id;
    this.name = name == null ? "" : name;
    this.description = description == null ? "" : description;
  }

  public long getId() { return id; }
  public String getName() { return name; }
  public String getDescription() { return description; }

  @Override
  public String toString() {
    return "TaigaProject{id=" + id + ", name='" + name + "'}";
  }
}