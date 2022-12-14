import java.util.*;

public class BoardEvent implements Event {
  public final Passenger p; public final Train t; public final Station s;
  public BoardEvent(Passenger p, Train t, Station s) {
    this.p = p; this.t = t; this.s = s;
  }
  public boolean equals(Object o) {
    if (o instanceof BoardEvent e) {
      return p.equals(e.p) && t.equals(e.t) && s.equals(e.s);
    }
    return false;
  }
  public int hashCode() {
    return Objects.hash(p, t, s);
  }
  public String toString() {
    return "Passenger " + p + " boards " + t + " at " + s;
  }
  public List<String> toStringList() {
    return List.of(p.toString(), t.toString(), s.toString());
  }
  public void replayAndCheck(MBTA mbta) {
    System.out.println(mbta.curr_mbta_state);
    System.out.println(mbta.lines);
    System.out.println(mbta.trips);
    if(!mbta.curr_mbta_state.isEmpty()) {

//      String curr_train_color = new ArrayList<>(s.train.keySet()).get(0).toString();
      if (mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s)).waiting_passengers.contains(p)) {
        if(mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s)).train.get(t) == null) {
          List<Passenger> new_passenger = new ArrayList<>();
          new_passenger.add(p);
          mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s)).train.put(t, new_passenger);
        } else {
          mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s)).train.get(t).add(p);
        }
        mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(s)).waiting_passengers.remove(p);
        mbta.passenger_trip_tracker.get(p.toString()).remove(s.toString());
      } else {
        throw new RuntimeException("Error with boarding");
      }
    } else {
      throw new RuntimeException();
    }
  }
}