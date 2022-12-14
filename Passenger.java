public class Passenger extends Entity {
  private Passenger(String name) { super(name); }

  public static Passenger make(String name) {
    if(!cache.containsKey(name) || !(cache.get(name) instanceof Passenger)) {
      cache.put(name, new Passenger(name));
    }
    return (Passenger) cache.get(name);
  }
}
