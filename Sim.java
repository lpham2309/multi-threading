import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sim {
  public static class PassengerThread implements Runnable{
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

    @Override
    public void run() {
      Passenger curr_passenger = passenger;
      while (!mbta.trips.get(curr_passenger.toString()).isEmpty()) {

        Station curr_station = null;
        Station next_train_station = null;
        Train curr_train = null;
        String next_passenger_station = "";

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


        if (curr_train != null) {

          if(mbta.lines.get(curr_train.toString()).indexOf(curr_station.toString()) < mbta.lines.get(curr_train.toString()).size() -1) {
            int next_idx = mbta.lines.get(curr_train.toString()).indexOf(curr_station.toString()) + 1;
            next_train_station = Station.make(mbta.lines.get(curr_train.toString()).get(next_idx));
          } else {
            int prev_idx = mbta.lines.get(curr_train.toString()).indexOf(curr_station.toString()) - 1;
            next_train_station = Station.make(mbta.lines.get(curr_train.toString()).get(prev_idx));
          }

          curr_station.lock.lock();

          while (curr_station.train.isEmpty()) {
            try {
              curr_station.cond.await();
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
          System.out.println(curr_station + " " + curr_station.train);
          System.out.println(curr_station.train.get(curr_train));



          System.out.println("Passenger: " + curr_passenger + " waiting for " + curr_train + " at " + curr_station);
          System.out.println(curr_train + " is moving to: " + next_train_station);


          if (!curr_station.train.isEmpty() && mbta.trips.get(curr_passenger.toString()).size() > 1
                  && mbta.trips.get(curr_passenger.toString()).contains(curr_station.toString())) {
            log.passenger_boards(curr_passenger, curr_train, curr_station);
            curr_station.passengers.remove(passenger);
            curr_station.train.get(curr_train).add(passenger);
            this.is_recent_onboard = true;

            // put synchronized if needed
            mbta.trips.get(curr_passenger.toString()).remove(curr_station.toString());

          }

          curr_station.cond.signalAll();
          curr_station.lock.unlock();


          next_train_station.lock.lock();
          while (!next_train_station.train.containsKey(curr_train)) {
            try {
              next_train_station.cond.await();
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }

          if ((mbta.trips.get(curr_passenger.toString()).size() == 1 && curr_station.toString().equals(mbta.trips.get(curr_passenger.toString()).get(0)))
          & !this.is_recent_onboard) {
            log.passenger_deboards(curr_passenger, curr_train, curr_station);
            return;
          }
          next_train_station.cond.signalAll();
          next_train_station.lock.unlock();
        }
      }
    }
  }

  public static class TrainThread implements Runnable {
    Train train;
    MBTA mbta;
    Log log;
    boolean is_forward = true;
    TrainThread(Train train, MBTA mbta, Log log) {
      this.train = train;
      this.mbta = mbta;
      this.log = log;
    }
    @Override
    public void run() {
      while(true) {
        Train curr_train = train;
        Station curr_station = null;
        Station next_station = null;

        for (Station s : mbta.curr_mbta_state) {
          if (s.train.keySet().contains(curr_train) && !s.train.isEmpty()) {
            curr_station = s;
          }
        }

        List<String> list_of_stations = mbta.lines.get(curr_train.toString());
        int curr_station_idx = 0;
        if (list_of_stations.size() != 0) {
          curr_station_idx = list_of_stations.indexOf(curr_station.toString());
        }


        if (is_forward && (list_of_stations.size() - curr_station_idx >= 2)) {
          curr_station = Station.make(list_of_stations.get(curr_station_idx));
          next_station = Station.make(list_of_stations.get(curr_station_idx + 1));
          curr_station_idx++;
        } else {
          is_forward = false;
          curr_station = Station.make(list_of_stations.get(curr_station_idx));
          next_station = Station.make(list_of_stations.get(curr_station_idx - 1));
          curr_station_idx--;
          if (curr_station_idx == 0) {
            is_forward = true;
          }
        }

        curr_station.lock.lock();
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          return;
        }

        next_station.lock.lock();

        try {
          while (!next_station.train.isEmpty()) {
            next_station.cond.await();
          }
        } catch (InterruptedException e) {
          return;
        }

        if(next_station.train.isEmpty()) {
//          log.train_moves(curr_train, curr_station, next_station);
          List<Passenger> curr_train_info = curr_station.train.get(curr_train);
          mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(curr_station)).train.clear();
          mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(next_station)).train.put(curr_train, curr_train_info);
          log.train_moves(curr_train, curr_station, next_station);
        }

        curr_station.cond.signalAll();
        next_station.cond.signalAll();

        next_station.lock.unlock();
        curr_station.lock.unlock();
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          return;
        }
      }
    }
  }
  public static void run_sim(MBTA mbta, Log log){
//    throw new RuntimeException();
    List<Thread> list_of_trains = new ArrayList<>();
    List<Thread> list_of_passengers = new ArrayList<>();
    List<Thread> all = new ArrayList<>();

    for(String train : mbta.lines.keySet()) {
      Runnable tt = new TrainThread(Train.make(train), mbta, log);
      Thread train_thread = new Thread(tt);
      list_of_trains.add(train_thread);
      all.add(train_thread);
//      train_thread.run();
//        train_thread.start();
    }

    for(String passenger : mbta.trips.keySet()) {
      Runnable tt = new PassengerThread(Passenger.make(passenger), mbta, log);
      Thread passenger_thread = new Thread(tt);
      list_of_passengers.add(passenger_thread);
      all.add(passenger_thread);
    }

    for(Thread tp : list_of_trains) {
        tp.start();
    }
//
    for(Thread tp : list_of_passengers) {
      tp.start();
    }

    for(Thread tp : list_of_passengers) {
      try {
        tp.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    for(Thread t : list_of_trains) {
      if(!t.isInterrupted()) {
        t.interrupt();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("usage: ./sim <config file>");
      System.exit(1);
    }

    MBTA mbta = new MBTA();
    mbta.loadConfig(args[0]);
//
    Log log = new Log();

    run_sim(mbta, log);

//    String s = new LogJson(log).toJson();
//    PrintWriter out = new PrintWriter("log.json");
//    out.print(s);
//    out.close();
//
//    mbta.reset();
//    mbta.loadConfig(args[0]);
//    Verify.verify(mbta, log);
  }
}