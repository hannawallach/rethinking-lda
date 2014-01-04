package edu.umass.cs.iesl.wallach.hierarchical;

public class Timer {

  public static void printTimingInfo(long start, long end) {

    long seconds = Math.round((end - start) / 1000.0);
    long minutes = seconds / 60; seconds %= 60;
    long hours = minutes / 60; minutes %= 60;
    long days = hours / 24; hours %= 24;

    System.out.print("\nTotal time: ");

    if (days != 0)
      System.out.print(days + " days ");
    if (hours != 0)
      System.out.print(hours + " hours ");
    if (minutes != 0)
      System.out.print(minutes + " minutes ");

    System.out.println(seconds + " seconds");
  }
}
