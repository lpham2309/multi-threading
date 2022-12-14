import java.util.*;

public class DeboardEvent implements Event {
  public final Passenger p; public final Train t; public final Station s;
  public DeboardEvent(Passenger p, Train t, Station s) {
    this.p = p; this.t = t; this.s = s;
  }
  public boolean equals(Object o) {
    if (o instanceof DeboardEvent e) {
      return p.equals(e.p) && t.equals(e.t) && s.equals(e.s);
    }
    return false;
  }
  public int hashCode() {
    return Objects.hash(p, t, s);
  }
  public String toString() {
    return "Passenger " + p + " deboards " + t + " at " + s;
  }
  public List<String> toStringList() {
    return List.of(p.toString(), t.toString(), s.toString());
  }
  public void replayAndCheck(MBTA mbta) {
    System.out.println(mbta.curr_mbta_state);
    System.out.println(mbta.lines);
    System.out.println(mbta.trips);
    if(!mbta.curr_mbta_state.isEmpty()) {
      String destination_station = mbta.trips.get(p.toString()).get(mbta.trips.get(p.toString()).size()-1);
      if (destination_station.equals(s.toString()) ||
              !mbta.lines.get(t.toString()).contains(mbta.trips.get(p.toString()).get(mbta.trips.get(p.toString()).indexOf(s.toString()) + 1))) {
        mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s)).passengers.add(p);
        if(mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s)).train.get(t) != null) {
          mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s)).train.get(t).remove(p);
        }
      } else {
        throw new RuntimeException();
      }
    } else {
      throw new RuntimeException();
    }
  }
}