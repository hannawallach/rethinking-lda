package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import cc.mallet.types.*;

// this does not handle resetting to training data counts etc.

public abstract class TopicScore extends HDScore {

  protected Level[] level;

  protected int T, D; // constants

  // the following invoke the relevant method on the bottom level

  public double getScore(int j, int d) {

    return this.level[0].getScore(new int[] { j, d });
  }

  public void incrementCounts(int j, int d) {

    this.level[0].incrementCounts(new int[] { j, d });
  }

  public void decrementCounts(int j, int d) {

    this.level[0].decrementCounts(new int[] { j, d });
  }

  public void resetCounts() {

    this.level[0].resetCounts();
  }

  // computes log P(z) using the predictive distribution

  public double logProb(InstanceList docs, int[][] z) {

    double logProb = 0.0;

    resetCounts();

    assert docs.size() == D;

    for (int d=0; d<D; d++) {

      FeatureSequence fs = (FeatureSequence) docs.get(d).getData();

      int nd = fs.getLength();

      for (int i=0; i<nd; i++) {

        int j = z[d][i];

        logProb += Math.log(getScore(j, d));

        incrementCounts(j, d);
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

      for (int j=0; j<T; j++) {

        double pseudoParam = level[0].getParam();
        pseudoParam *= level[1].getScore(new int[] { j });

        pw.println(j + "\t" + pseudoParam);
      }

      pw.close();
    }
    catch (IOException e) {
      System.out.println(e);
    }
  }

  public void print(InstanceList docs, double limit, int num, String file) {

    assert docs.size() == D;

    try {

      PrintWriter pw = new PrintWriter(file, "UTF-8");

      pw.println("#doc source topic proportion ...");

      Probability[] probs = new Probability[T];

      for (int d=0; d<D; d++) {

        pw.print(d); pw.print(" ");
        pw.print(docs.get(d).getSource()); pw.print(" ");

        for (int j=0; j<T; j++)
          probs[j] = new Probability(j, getScore(j, d));

        Arrays.sort(probs);

        if ((num > T) || (num < 0))
          num = T;

        for (int i=0; i<num; i++) {

          // break if there are no more topics whose proportion is
          // greater than zero or limit...

          if ((probs[i].prob == 0) || (probs[i].prob < limit))
            break;

          pw.print(probs[i].index); pw.print(" ");
          pw.print(probs[i].prob); pw.print(" ");
        }

        pw.println();
      }

      pw.close();
    }
    catch (IOException e) {
      System.out.println(e);
    }
  }
}
