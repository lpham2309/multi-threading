public class Train extends Entity {
  private Train(String name) { super(name); }

  public static Train make(String name) {
    if(!cache.containsKey(name) || !(cache.get(name) instanceof Train)) {
      cache.put(name, new Train(name));
    }
    return (Train) cache.get(name);
  }
}
