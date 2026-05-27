package javiergs.tulip.taiga;

public class TaigaUserStory {
  private final long id;
  private final int ref;
  private final String subject;
  private final int statusId;
  private final Long milestoneId;
  private final long version;

  public TaigaUserStory(long id, int ref, String subject, int status,
                        Long milestoneId, long version) {
    this.id = id;
    this.ref = ref;
    this.subject = subject;
    this.statusId = status;
    this.milestoneId = milestoneId;
    this.version = version;
  }

  public long getId() { return id; }
  public int getRef() { return ref; }
  public String getSubject() { return subject; }
  public int getStatusId() { return statusId; }

  @Override
  public String toString() {
    return "#" + ref + " " + subject;
  }

  public Long getMilestoneId() {
    return milestoneId;
  }

  public long getVersion() {
    return version;
  }
}