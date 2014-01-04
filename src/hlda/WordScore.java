package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import cc.mallet.types.*;

// this does not handle resetting to training data counts etc.

public abstract class WordScore extends HDScore {

  protected Level[] level;

  protected int W, T; // constants

  // the following invoke the relevant method on the bottom level

  public double getScore(int w, int j) {

    return this.level[0].getScore(new int[] { w, j });
  }

  public void incrementCounts(int w, int j) {

    this.level[0].incrementCounts(new int[] { w, j });
  }

  public void decrementCounts(int w, int j) {

    this.level[0].decrementCounts(new int[] { w, j });
  }

  public void resetCounts() {

    this.level[0].resetCounts();
  }

  // computes log P(z) using the predictive distribution

  public double logProb(InstanceList docs, int[][] z) {

    double logProb = 0.0;

    resetCounts();

    int D = docs.size();

    for (int d=0; d<D; d++) {

      FeatureSequence fs = (FeatureSequence) docs.get(d).getData();

      int nd = fs.getLength();

      for (int i=0; i<nd; i++) {

        int w = fs.getIndexAtPosition(i);
        int j = z[d][i];

        logProb += Math.log(getScore(w, j));

        incrementCounts(w, j);
      }
    }

    return logProb;
  }

  public double[] getParam() {

    double[] param = new double[level.length - 1];

    for (int i=0; i<param.length; i++)
      param[i] = level[i].getParam();

    return param;
  }

  public void setParam(double[] param) {

    assert param.length == level.length - 1;

    for (int i=0; i<param.length; i++)
      level[i].setParam(param[i]);
  }

  public void printPseudoParam(String file) {

    try {

      PrintWriter pw = new PrintWriter(file, "UTF-8");

      for (int w=0; w<W; w++) {

        double pseudoParam = level[0].getParam();
        pseudoParam *= level[1].getScore(new int[] { w });

        pw.println(w + "\t" + pseudoParam);
      }

      pw.close();
    }
    catch (IOException e) {
      System.out.println(e);
    }
  }

  public void print(Alphabet dict, double limit, int num, String file) {

    try {

      PrintWriter pw = null;

      if (file == null)
        pw = new PrintWriter(System.out, true);
      else {
        pw = new PrintWriter(file, "UTF-8");
        pw.println("topic typeindex type proportion");
      }

      Probability[] probs = new Probability[W];

      for (int j=0; j<T; j++) {
        for (int w=0; w<W; w++)
          probs[w] = new Probability(w, getScore(w, j));

        Arrays.sort(probs);

        if ((num > W) || (num < 0))
          num = W;

        StringBuffer line = new StringBuffer();

        for (int i=0; i<num; i++) {

          // break if there are no more words whose proportion is
          // greater than zero or limit...

          if ((probs[i].prob == 0) || (probs[i].prob < limit))
            break;

          if (file == null) {
            line.append(dict.lookupObject(probs[i].index));
            line.append(" ");
          }
          else {
            pw.print(j); pw.print(" ");
            pw.print(probs[i].index); pw.print(" ");
            pw.print(dict.lookupObject(probs[i].index)); pw.print(" ");
            pw.print(probs[i].prob); pw.println();
          }
        }

        String string = line.toString();

        if (file == null)
          if (!string.equals(""))
            pw.println("Topic " + j + ": " + string);
      }

      if (file != null)
        pw.close();
    }
    catch (IOException e) {
      System.out.println(e);
    }
  }
}
