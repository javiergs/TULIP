package javiergs.tulip.taiga;

public class TaigaUserStory {
  private final long id;
  private final int ref;
  private final String subject;
  private final int statusId;

  public TaigaUserStory(long id, int ref, String subject, int statusId) {
    this.id = id;
    this.ref = ref;
    this.subject = subject == null ? "" : subject;
    this.statusId = statusId;
  }

  public long getId() { return id; }
  public int getRef() { return ref; }
  public String getSubject() { return subject; }
  public int getStatusId() { return statusId; }

  @Override
  public String toString() {
    return "#" + ref + " " + subject;
  }
}