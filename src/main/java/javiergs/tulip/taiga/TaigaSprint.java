package javiergs.tulip.taiga;

public class TaigaSprint {
  private final long id;
  private final String name;
  private final String startDate;
  private final String endDate;

  public TaigaSprint(long id, String name, String startDate, String endDate) {
    this.id = id;
    this.name = name == null ? "" : name;
    this.startDate = startDate == null ? "" : startDate;
    this.endDate = endDate == null ? "" : endDate;
  }

  public long getId() { return id; }
  public String getName() { return name; }
  public String getStartDate() { return startDate; }
  public String getEndDate() { return endDate; }

  @Override
  public String toString() {
    return name + " (" + startDate + " to " + endDate + ")";
  }
}