package javiergs.tulip.taiga;

public class TaigaUserStory {
  private final long id;
  private final int ref;
  private final String subject;
  private final int statusId;
  private final Long milestoneId;

  public TaigaUserStory(long id, int ref, String subject, int statusId, Long milestoneId) {
    this.id = id;
    this.ref = ref;
    this.subject = subject == null ? "" : subject;
    this.statusId = statusId;
    this.milestoneId = milestoneId;
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