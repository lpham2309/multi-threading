import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MBTA {
  public Map<String, List<String>> lines;
  // Do synchronized //
  public Map<String, List<String>> trips;
  public List<Station> curr_mbta_state;
  public Map<String, List<String>> passenger_trip_tracker;
  // Creates an initially empty simulation
  public MBTA() {
    this.lines = new HashMap<>();
    this.trips = new HashMap<>();
    this.curr_mbta_state = new ArrayList<>();
    this.passenger_trip_tracker = new HashMap<>();

    reset();
  }

  public List<Station> updateState(){
    return this.curr_mbta_state;
  }

  // Adds a new transit line with given name and stations
  public void addLine(String name, List<String> stations) {
    System.out.println("Calling addLine");
    if(!this.lines.containsKey(name)) {
      this.lines.put(name, stations);
    }

    Map<String, List<String>> new_line = new HashMap<>();
    new_line.put(name, stations);

    for(Map.Entry<String, List<String>> entry : new_line.entrySet()) {
      for(int i = 0; i < entry.getValue().size(); i++) {
        Station s = Station.make(entry.getValue().get(i));
        if(!this.curr_mbta_state.contains(s)) {
          this.curr_mbta_state.add(s);
        }
      }
    }

    for(Map.Entry<String, List<String>> entry : new_line.entrySet()) {
      String initial_station_each_line = entry.getValue().get(0);
      Station initial_station = Station.make(initial_station_each_line);
      Train initial_train = Train.make(entry.getKey());
      this.curr_mbta_state.get(this.curr_mbta_state.indexOf(initial_station)).train.put(initial_train, new ArrayList<>());
    }
  }

  // Adds a new planned journey to the simulation
  public void addJourney(String name, List<String> stations) {
    System.out.println("Calling addJourney");
    if(!this.trips.containsKey(name)) {
      this.trips.put(name, stations);
    }
    Map<String, List<String>> new_journey = new HashMap<>();
    new_journey.put(name, stations);

    for(Map.Entry<String, List<String>> entry : new_journey.entrySet()){
      Passenger passenger = Passenger.make(entry.getKey());
      String initial_station_each_line = entry.getValue().get(0);
      Station initial_station = Station.make(initial_station_each_line);
      if(!this.curr_mbta_state.get(this.curr_mbta_state.indexOf(initial_station)).train.isEmpty()){
        Set<Train> curr_train = this.curr_mbta_state.get(this.curr_mbta_state.indexOf(initial_station)).train.keySet();
        List<Train> converted_curr_train = new ArrayList<>(curr_train);
//        this.curr_mbta_state.get(this.curr_mbta_state.indexOf(initial_station)).train.get(converted_curr_train.get(0)).add(passenger);
        if(!this.curr_mbta_state.get(this.curr_mbta_state.indexOf(initial_station)).passengers.contains(passenger)){
          this.curr_mbta_state.get(this.curr_mbta_state.indexOf(initial_station)).passengers.add(passenger);
        }
      }
    }
  }

  // Return normally if initial simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkStart() {
    if(!this.curr_mbta_state.isEmpty()) {
      for (Station s : this.curr_mbta_state) {
        if (!s.train.isEmpty()) {
          List<Train> curr_train = new ArrayList<>(s.train.keySet());

          String intended_station = this.lines.get(curr_train.get(0).toString()).get(0);
          String curr_station = this.curr_mbta_state.get(this.curr_mbta_state.indexOf(s)).toString();
//          String b = this.curr_mbta_state.get(s).train;

          if (!intended_station.equals(curr_station)) {
            System.out.println("1");
            throw new RuntimeException();
          }
          for (Passenger p : s.passengers) {
            if (s.passengers.size() == 0 && !s.toString().equals(this.trips.get(p.toString()).get(0))) {
              System.out.println("2");
              throw new RuntimeException();
            }
          }
        }
      }
    } else if(this.trips.isEmpty() && this.lines.isEmpty() && this.curr_mbta_state.isEmpty()){}
    else {
      throw new RuntimeException();
    }
  }

  // Return normally if final simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkEnd() {
    if(!this.curr_mbta_state.isEmpty()) {
      for(Map.Entry<String, List<String>> entry : this.passenger_trip_tracker.entrySet()) {
        if(entry.getValue().size() != 0) {
          throw new RuntimeException();
        }
      }
    } else {
      throw new RuntimeException();
    }
  }

  // reset to an empty simulation
  public void reset() {
    this.lines.clear();
    this.trips.clear();
    this.curr_mbta_state.clear();
    Station.cache.clear();
    Passenger.cache.clear();
    Train.cache.clear();
  }

  // adds simulation configuration from a file
  public void loadConfig(String filename) {
    try {
      Gson gson = new Gson();
      Reader reader = Files.newBufferedReader(Paths.get(filename));
      JsonParser decoded = gson.fromJson(reader, new TypeToken<JsonParser>(){}.getType());
      this.lines.putAll(decoded.lines);
      this.trips.putAll(decoded.trips);

      this.passenger_trip_tracker.putAll(decoded.trips);

      for(Map.Entry<String, List<String>> entry : this.lines.entrySet()) {
        for(int i = 0; i < entry.getValue().size(); i++) {
          Station s = Station.make(entry.getValue().get(i));
          if(!this.curr_mbta_state.contains(s)) {
            this.curr_mbta_state.add(s);
          }
        }
      }

      for(Map.Entry<String, List<String>> entry : this.lines.entrySet()) {
        String initial_station_each_line = entry.getValue().get(0);
        Station initial_station = Station.make(initial_station_each_line);
        Train initial_train = Train.make(entry.getKey());
        this.curr_mbta_state.get(this.curr_mbta_state.indexOf(initial_station)).train.put(initial_train, new ArrayList<>());
      }

      for(Map.Entry<String, List<String>> entry : this.trips.entrySet()){
        Passenger passenger = Passenger.make(entry.getKey());
        String initial_station_each_line = entry.getValue().get(0);
        Station initial_station = Station.make(initial_station_each_line);
        if(!this.curr_mbta_state.get(this.curr_mbta_state.indexOf(initial_station)).passengers.contains(passenger)){
          this.curr_mbta_state.get(this.curr_mbta_state.indexOf(initial_station)).passengers.add(passenger);
        }
      }
      System.out.println(this.curr_mbta_state);
      for(Station s : this.curr_mbta_state){
        System.out.println(s + " " + s.train);
        System.out.println(s + " " + s.passengers);
      }
      System.out.println(this.lines);
      System.out.println(this.trips);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}