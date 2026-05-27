package javiergs.tulip.taiga;

public class TaigaTask {
  private final long id;
  private final int ref;
  private final String subject;
  private final int statusId;
  private final Long assignedToUserId; // nullable
  private final Long userStoryId;
  private final long version;

  public TaigaTask(long id, int ref, String subject, int status,
                   Long assignedToUserId, Long userStoryId, long version) {
    this.id = id;
    this.ref = ref;
    this.subject = subject;
    this.statusId = status;
    this.assignedToUserId = assignedToUserId;
    this.userStoryId = userStoryId;
    this.version = version;
  }

  public Long getUserStoryId() {
    return userStoryId;
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
  public long getVersion() {
    return version;
  }

}