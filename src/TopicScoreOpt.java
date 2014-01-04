package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import gnu.trove.*;
import cc.mallet.types.*;
import cc.mallet.util.Maths;

// this does not handle resetting to training data counts etc.

public class TopicScoreOpt {

  // observed counts

  private int[][] topicDocCounts; // N_{j|d}
  private int[] topicDocCountsNorm; // N_{.|d}

  private int[][] topicDocHist;
  private int[] topicDocNormHist;

  private int T, D; // constants

  // hyperparamters

  private double[] param;
  private double paramSum;

  // create a score function with zero counts

  public TopicScoreOpt(int T, int D, double paramSum) {

    this.T = T;
    this.D = D;

    this.param = new double[T];
    Arrays.fill(this.param, paramSum / T);

    this.paramSum = paramSum;

    // allocate space for counts

    topicDocCounts = new int[T][D];
    topicDocCountsNorm = new int[D];
  }

  public double getScore(int j, int d) {

    int njd = topicDocCounts[j][d];
    int nd = topicDocCountsNorm[d];

    return (njd + param[j]) / (nd + paramSum);
  }

  public void incrementCounts(int j, int d) {

    topicDocCounts[j][d]++;
    topicDocCountsNorm[d]++;
  }

  public void decrementCounts(int j, int d) {

    topicDocCounts[j][d]--;
    topicDocCountsNorm[d]--;
  }

  public void resetCounts() {

    for (int j=0; j<T; j++)
      Arrays.fill(topicDocCounts[j], 0);

    Arrays.fill(topicDocCountsNorm, 0);
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

    return param;
  }

  public double getParamSum() {

    return paramSum;
  }

  public void printParam(String file) {

    double[] param = getParam();

    try {

      PrintWriter pw = new PrintWriter(file, "UTF-8");

      for (int i=0; i<param.length; i++)
        pw.println(param[i]);

      pw.close();
    }
    catch (IOException e) {
      System.out.println(e);
    }
  }

  public void initializeHists(int ndMax) {

    topicDocHist = new int[T][ndMax + 1];
    topicDocNormHist = new int[ndMax + 1];

    for (int d=0; d<D; d++)
      topicDocNormHist[topicDocCountsNorm[d]]++;
  }

  public void optimizeParam(int numItns) {

    // update histograms and set the max # of times that each topic
    // has occurred in any document...

    int[] limits = new int[T];
    Arrays.fill(limits, -1);

    for (int j=0; j<T; j++) {

      Arrays.fill(topicDocHist[j], 0);

      for (int d=0; d<D; d++)
        topicDocHist[j][topicDocCounts[j][d]]++;

      for (int n=0; n<topicDocHist[j].length; n++)
        if (topicDocHist[j][n] > 0)
          limits[j] = n;
    }

    // optimize hyperparameters

    for (int i=0; i<numItns; i++) {

      double denominator = 0.0;
      double currentDigamma = 0.0;

      for (int n=1; n<topicDocNormHist.length; n++) {
        currentDigamma += 1.0 / (paramSum + n - 1);
        denominator += topicDocNormHist[n] * currentDigamma;
      }

      paramSum = 0.0;

      for (int j=0; j<T; j++) {

        int limit = limits[j];

        double oldParam = param[j];
        param[j] = 0.0;
        currentDigamma = 0;

        for (int n=1; n<=limit; n++) {
          currentDigamma += 1.0 / (oldParam + n - 1);
          param[j] += topicDocHist[j][n] * currentDigamma;
        }

        param[j] *= oldParam / denominator;

        if (param[j] == 0.0)
          param[j] = 1e-10;

        paramSum += param[j];
      }
    }
  }

  private double digamma(double x) {

    x = x + 6;

    double p = 1.0 / (x * x);

    p = (((0.004166666666667 * p - 0.003968253986254) * p + 0.008333333333333) * p - 0.083333333333333) * p;
    p = p + Math.log(x) - 0.5 / x - 1 / (x-1) - 1 / (x-2) - 1 / (x-3) - 1 / (x-4) - 1 / (x-5) - 1 / (x-6);

    return p;
  }

  public void optimizeParamSum(int numItns) {

    for (int i=0; i<numItns; i++) {

      double q = 0.0;
      double digammaParamSum = digamma(paramSum);

      for (int d=0; d<D; d++)
        q += digamma(topicDocCountsNorm[d] + paramSum) - digammaParamSum;

      double p = 0.0;

      for (int j=0; j<T; j++) {

        double digammaParam = digamma(param[j]);

        for (int d=0; d<D; d++)
          p += (digamma(topicDocCounts[j][d] + param[j]) - digammaParam) / T;
      }

      paramSum *= p / q;

      for (int j=0; j<T; j++)
        param[j] = paramSum / T;
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
