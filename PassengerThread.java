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
    PassengerThread(Passenger passenger, MBTA mbta, Log log) {
        this.mbta = mbta;
        this.passenger = passenger;
        this.log = log;
        this.curr_passenger_trips = mbta.trips.get(passenger.toString());
    }

    private Object[] getTrainAndStation(Passenger curr_passenger){
        Station curr_station = null;
        Train curr_train = null;

        // Get the current station
        for (Station station : mbta.curr_mbta_state) {
            if (station.waiting_passengers.contains(curr_passenger)) {
                curr_station = station;
                break;
            }
        }

        // If passenger not at station
        if (curr_station == null) {
            // Check if passenger on train
            for (Station station : mbta.curr_mbta_state) {
                if (!station.train.isEmpty()) {
                    for (var entry : station.train.entrySet()) {
                        if (entry.getValue().contains(curr_passenger)) {
                            curr_station = station;
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

        return new Object[] { curr_station, curr_train };
    }

    private Station getNextStation(Train curr_train, Station curr_station){
        if(mbta.lines.get(curr_train.toString()).indexOf(curr_station.toString()) < mbta.lines.get(curr_train.toString()).size() -1) {
            int next_idx = mbta.lines.get(curr_train.toString()).indexOf(curr_station.toString()) + 1;
            return Station.make(mbta.lines.get(curr_train.toString()).get(next_idx));
        } else {
            int prev_idx = mbta.lines.get(curr_train.toString()).indexOf(curr_station.toString()) - 1;
            return Station.make(mbta.lines.get(curr_train.toString()).get(prev_idx));
        }
    }

    private boolean shouldBoardTrain(Station curr_station, Train curr_train){
        return !curr_station.train.isEmpty()
                && curr_station.boardingTrain == curr_train
                && mbta.trips.get(passenger.toString()).size() > 1
                && mbta.trips.get(passenger.toString()).contains(curr_station.toString());
    }

    private boolean shouldDeboardTrain(Station curr_station){
        var currLocation = mbta.trips.get(passenger.toString()).get(0);
        var nextDestination = mbta.trips.get(passenger.toString()).get(1);
        var stationsNeedsToReach = mbta.trips.get(passenger.toString());

        System.out.println("Trips at MBTA: " + mbta.trips);
        System.out.println("Stations left: " + stationsNeedsToReach);
        System.out.println("Curr des left: " + currLocation);
        System.out.println("Next des left: " + nextDestination);

        // if passenger is on
        return ((
                stationsNeedsToReach.size() > 1
                && !mbta.lines.get(curr_station.boardingTrain.toString()).contains(nextDestination)
        ) || (
                stationsNeedsToReach.size() == 1
                && stationsNeedsToReach.get(0) == curr_station.toString())
        );
    }

    @Override
    public void run() {
        Passenger curr_passenger = passenger;
        while (!mbta.trips.get(curr_passenger.toString()).isEmpty()) {
            Object[] trainAndStation = getTrainAndStation(passenger);
            Station curr_station = (Station) trainAndStation[0];
            Train curr_train = (Train) trainAndStation[1];

            String next_passenger_station = "";
            Station next_train_station = null;

            if (curr_train != null) {
                // get next station
                next_train_station = getNextStation(curr_train, curr_station);

                curr_station.lock.lock();

                while (curr_station.train.isEmpty()) {
                    try {
                        curr_station.cond.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("Train at station:" + curr_station.boardingTrain);
                System.out.println(curr_station + " " + curr_station.train);
                System.out.println("Passenger on Train: "+curr_station.train.get(curr_train));
                System.out.println(curr_train + " is moving to: " + next_train_station);


                // BOARDING
                if (shouldBoardTrain(curr_station, curr_train)) {
                    System.out.println("Passenger: " + curr_passenger + " waiting for " + curr_train + " at " + curr_station);
                    log.passenger_boards(curr_passenger, curr_train, curr_station);
                    curr_station.waiting_passengers.remove(passenger);
                    System.out.println("Boarding train: " + curr_train);
                    curr_station.train.get(curr_train).add(passenger);

                    // put synchronized if needed
                    mbta.trips.get(curr_passenger.toString()).remove(curr_station.toString());

                    curr_station.cond.signalAll();
                    curr_station.lock.unlock();
                    continue;
                }

                // DEBOARDING
                if (shouldDeboardTrain(curr_station)) {
                    log.passenger_deboards(curr_passenger, curr_train, curr_station);
                    curr_station.waiting_passengers.add(passenger);
                    var boardedPassengers = mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(curr_station)).train.get(curr_station.boardingTrain);
                    boardedPassengers.remove(passenger);
                    System.out.println("Passengers still on train" + mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(curr_station)).train.get(curr_station.boardingTrain));


                    curr_station.cond.signalAll();
                    curr_station.lock.unlock();
                    continue;
                }

                System.out.println("");
            }

        }
    }
}
