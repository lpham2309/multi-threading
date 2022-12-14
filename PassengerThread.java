import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PassengerThread implements Runnable{
    Passenger passenger;
    MBTA mbta;
    Log log;
    List<String> curr_passenger_trips;
    boolean is_recent_onboard = false;
    boolean should_onboard_newline = false;
    Train curr_train_tracker = null;
    Station curr_station_tracker = null;

    Station next_train_station;
    PassengerThread(Passenger passenger, MBTA mbta, Log log) {
        this.mbta = mbta;
        this.passenger = passenger;
        this.log = log;
        this.curr_passenger_trips = mbta.trips.get(passenger.toString());
    }

    @Override
    public void run() {
        Passenger curr_passenger = passenger;
        while (!curr_passenger_trips.isEmpty()) {
            String next_passenger_station = "";
            Train curr_train = null;
            Station curr_station = null;

            // Get the current station
            for (Station s : mbta.curr_mbta_state) {
                if (s.passengers.contains(curr_passenger)) {
                    curr_station = s;
                    break;
                }
            }

            if (curr_station == null) {
                for (Station s : mbta.curr_mbta_state) {
                    if (!s.train.isEmpty()) {
                        for (Map.Entry<Train, List<Passenger>> entry : s.train.entrySet()) {
                            if (entry.getValue().contains(curr_passenger)) {
                                curr_station = s;
                                curr_train = entry.getKey();
                                break;
                            }
                        }
                    }
                }
            }

            // Get the correct current waiting train
            for(Map.Entry<String, List<String>> entry : mbta.lines.entrySet()) {
                String next_station_passenger = mbta.trips.get(curr_passenger.toString()).get(mbta.trips.get(curr_passenger.toString()).indexOf(curr_station.toString()) + 1);
                if(entry.getValue().contains(next_station_passenger)) {
                    should_onboard_newline = true;
                    curr_train = Train.make(entry.getKey());
                }
            }
            Station next_passenger_trip = null;
            if(next_passenger_trip == null) {
                next_passenger_trip = Station.make(mbta.trips.get(curr_passenger.toString()).get(mbta.trips.get(curr_passenger.toString()).indexOf(curr_station.toString()) + 1));
            }


            curr_station.lock.lock();

            List<Passenger> curr_train_passenger = new ArrayList<>();
            for(List<Passenger> s : curr_station.train.values()) {
                for(Passenger i : s) {
                    curr_train_passenger.add(i);
                }
            }

            if(curr_station.passengers.size() > 0) {
                while (curr_station.train.isEmpty()) {
                    try {
                        curr_station.cond.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                // Check if the next passenger station is on the train path
                if(mbta.lines.get(curr_train.toString()).contains(next_passenger_trip.toString())) {
                    log.passenger_boards(curr_passenger, curr_train, curr_station);
                    System.out.println(curr_station);
                    mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(curr_station)).passengers.remove(passenger);
                    List<Passenger> passengers = new ArrayList<>();
                    passengers.add(passenger);
                    mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(curr_station)).train.put(curr_train, passengers);
//                    curr_station.passengers.remove(passenger);
//                    curr_station.train.get(curr_train).add(passenger);
                    curr_passenger_trips.remove(curr_station.toString());
                }

                /// logic for onboarding



            } else if(!curr_station.train.isEmpty() && curr_train_passenger.contains(curr_passenger)){
                // deboarding

                if(mbta.trips.get(curr_passenger.toString()).contains(next_passenger_trip.toString())) {
                    log.passenger_deboards(curr_passenger, curr_train, next_passenger_trip);

                    mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(curr_station)).passengers.add(curr_passenger);
                    mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(curr_station)).train.clear();
                    next_passenger_trip = null;

                    return;
                }
            }
                curr_station.cond.signalAll();
                curr_station.lock.unlock();
        }
    }
}