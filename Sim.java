import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sim {

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
