package javiergs.tulip.taiga;

public class TaigaTask {
  private final long id;
  private final int ref;
  private final String subject;
  private final int statusId;
  private final Long assignedToUserId; // nullable

  public TaigaTask(long id, int ref, String subject, int statusId, Long assignedToUserId) {
    this.id = id;
    this.ref = ref;
    this.subject = subject == null ? "" : subject;
    this.statusId = statusId;
    this.assignedToUserId = assignedToUserId;
  }

  public long getId() { return id; }
  public int getRef() { return ref; }
  public String getSubject() { return subject; }
  public int getStatusId() { return statusId; }
  public Long getAssignedToUserId() { return assignedToUserId; }

  @Override
  public String toString() {
    return "#" + ref + " " + subject;
  }
}