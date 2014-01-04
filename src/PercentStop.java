package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import gnu.trove.*;
import cc.mallet.types.*;
import cc.mallet.util.Maths;

public class PercentStop {

  public static void main(String[] args) throws java.io.IOException {

    if (args.length != 2) {
      System.out.println("Usage: PercentStop <stop_words> <topics>");
      System.exit(1);
    }

    String stopFile = args[0];
    String topicsFile = args[1];

    Alphabet dict = new Alphabet();

    try {

      BufferedReader in = new BufferedReader(new FileReader(stopFile));

      String line = null;

      while ((line = in.readLine()) != null) {

        String[] fields = line.split("\\s+");
        assert fields.length == 1;

        dict.lookupIndex(fields[0]);
      }

      in.close();
    }
    catch (IOException e) {
      System.err.println(e);
    }

    double totalStop = 0, total = 0;

    int[] hist = null;

    try {

      BufferedReader in = new BufferedReader(new FileReader(topicsFile));

      String line = null;

      while ((line = in.readLine()) != null) {

        String[] fields = line.split("\\s+");
        assert fields.length > 2;

        if (hist == null)
          hist = new int[fields.length - 1];

        int stop = 0, num = 0;

        for (int i=2; i<fields.length; i++) {
          if (dict.contains(fields[i]))
            stop++;

          num++;
        }

        hist[stop]++;

        totalStop += stop;
        total += num;
      }

      in.close();
    }
    catch (IOException e) {
      System.err.println(e);
    }

    System.out.println(totalStop / total);
  }
}
