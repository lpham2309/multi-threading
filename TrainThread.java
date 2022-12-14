import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrainThread implements Runnable {
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
          mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(curr_station)).boardingTrain = null;
          mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(next_station)).train.put(curr_train, curr_train_info);
          mbta.curr_mbta_state.get(mbta.curr_mbta_state.indexOf(next_station)).boardingTrain = curr_train;
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
