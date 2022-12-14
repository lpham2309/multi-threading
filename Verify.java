import java.io.*;
import java.util.*;

public class Verify {

  public static void verify(MBTA mbta, Log log) {
    mbta.checkStart();
    System.out.println("CHECKING START");
    for (Event e : log.events()) {;
      e.replayAndCheck(mbta);
    }
    mbta.checkEnd();
    System.out.println("CHECKING END");
  }

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length != 2) {
      System.out.println("usage: ./verify <config file> <log file>");
      System.exit(1);
    }

    MBTA mbta = new MBTA();
    mbta.loadConfig("sample.json");
    Reader r = new BufferedReader(new FileReader(args[1]));
    Log log = LogJson.fromJson(r).toLog();
    verify(mbta, log);
  }
}
