import java.util.*;

public class MoveEvent implements Event {
  public final Train t; public final Station s1, s2;
  public MoveEvent(Train t, Station s1, Station s2) {
    this.t = t; this.s1 = s1; this.s2 = s2;
  }
  public boolean equals(Object o) {
    if (o instanceof MoveEvent e) {
      return t.equals(e.t) && s1.equals(e.s1) && s2.equals(e.s2);
    }
    return false;
  }
  public int hashCode() {
    return Objects.hash(t, s1, s2);
  }
  public String toString() {
    return "Train " + t + " moves from " + s1 + " to " + s2;
  }
  public List<String> toStringList() {
    return List.of(t.toString(), s1.toString(), s2.toString());
  }
  public void replayAndCheck(MBTA mbta) {
    System.out.println(mbta.curr_mbta_state);
    System.out.println(mbta.lines);
    System.out.println(mbta.trips);
    if (!mbta.curr_mbta_state.isEmpty()) {
      if ((mbta.lines.get(t.toString()).indexOf(s2.toString()) - mbta.lines.get(t.toString()).indexOf(s1.toString())) > 1 ||
              (mbta.lines.get(t.toString()).indexOf(s2.toString()) - mbta.lines.get(t.toString()).indexOf(s1.toString())) < -1) {
        throw new RuntimeException();
      } else if (s1.equals(s2)) {
        throw new RuntimeException();
      } else if (!mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s1)).train.containsKey(t)) {
        throw new RuntimeException();
      } else if (s2.train.isEmpty()) {
        Station next_train = mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s2));
        Station curr_train = mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s1));
        Map<Train, List<Passenger>> next_train_info = next_train.train;
        Map<Train, List<Passenger>> curr_train_info = curr_train.train;
        mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s2)).train.putAll(curr_train_info);
        mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s1)).train.clear();
      } else if (s2.train.isEmpty()) {
        throw new RuntimeException();
      }
    } else if(mbta.curr_mbta_state.isEmpty()){}
    else {
      throw new RuntimeException();
    }
  }
}