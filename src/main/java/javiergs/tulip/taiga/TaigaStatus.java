package javiergs.tulip.taiga;

public class TaigaStatus {

  private final long id;
  private final String name;
  private final String slug;
  private final String color;
  private final int order;

  public TaigaStatus(long id, String name, String slug, String color, int order) {
    this.id = id;
    this.name = name;
    this.slug = slug;
    this.color = color;
    this.order = order;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSlug() {
    return slug;
  }

  public String getColor() {
    return color;
  }

  public int getOrder() {
    return order;
  }

  @Override
  public String toString() {
    return "TaigaStatus{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", slug='" + slug + '\'' +
        ", color='" + color + '\'' +
        ", order=" + order +
        '}';
  }
}