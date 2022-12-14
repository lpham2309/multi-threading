import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Station extends Entity {
  Lock lock = new ReentrantLock();
  Condition cond = lock.newCondition();
  Map<Train, List<Passenger>> train = new HashMap<>();
  List<Passenger> passengers = new ArrayList<>();
  private Station(String name) {
    super(name);
  }

  public static Station make(String name) {
    if(!cache.containsKey(name) || !(cache.get(name) instanceof Station)) {
      cache.put(name, new Station(name));
    }
    return (Station) cache.get(name);
  }
}
