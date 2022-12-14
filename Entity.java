import java.util.HashMap;
import java.util.Map;

public abstract class Entity {
  private final String name;
  protected static Map<String, Object> cache = new HashMap<>();

  protected Entity(String name) { this.name = name; }

  public String toString() { return name; }

  public boolean equals(Object o) {
    if (o instanceof Entity e) {
      return e.getClass().equals(getClass()) && name.equals(e.name);
    }
    return false;
  }

  public int hashCode() { return name.hashCode(); }
}
