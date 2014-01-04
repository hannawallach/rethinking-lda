package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import gnu.trove.*;
import cc.mallet.types.*;
import cc.mallet.util.Maths;

public class LDA {

  // observed counts

  private WordScoreOpt wordScore;
  private TopicScoreOpt topicScore;

  private int W, T, D; // constants

  private int[][] z; // topic assignments

  private LogRandoms rng; // random number generator

  private double getScore(int w, int j, int d) {

    return wordScore.getScore(w, j) * topicScore.getScore(j, d);
  }

  // computes P(w, z) using the predictive distribution

  private double logProb(InstanceList docs) {

    double logProb = 0;

    wordScore.resetCounts();
    topicScore.resetCounts();

    for (int d=0; d<D; d++) {

      FeatureSequence fs = (FeatureSequence) docs.get(d).getData();

      int nd = fs.getLength();

      for (int i=0; i<nd; i++) {

        int w = fs.getIndexAtPosition(i);
        int j = z[d][i];

        logProb += Math.log(getScore(w, j, d));

        wordScore.incrementCounts(w, j, false);
        topicScore.incrementCounts(j, d);
      }
    }

    return logProb;
  }

  private void sampleTopics(InstanceList docs, boolean init) {

    // resample topics

    int ndMax = -1;

    int[] wordCounts = (init) ? new int[W] : null;

    for (int d=0; d<D; d++) {

      FeatureSequence fs = (FeatureSequence) docs.get(d).getData();

      int nd = fs.getLength();

      if (init) {

        z[d] = new int[nd];

        if (nd > ndMax)
          ndMax = nd;
      }

      for (int i=0; i<nd; i++) {

        int w = fs.getIndexAtPosition(i);
        int oldTopic = z[d][i];

        if (!init) {
          wordScore.decrementCounts(w, oldTopic, !init);
          topicScore.decrementCounts(oldTopic, d);
        }

        // build a distribution over topics

        double dist[] = new double[T];
        double distSum = 0.0;

        for (int j=0; j<T; j++) {

          double score = getScore(w, j, d);

          dist[j] = score;
          distSum += score;
        }

        int newTopic = rng.nextDiscrete(dist, distSum);

        z[d][i] = newTopic;

        wordScore.incrementCounts(w, newTopic, !init);
        topicScore.incrementCounts(newTopic, d);

        if (init)
          wordCounts[w]++;
      }
    }

    if (init) {
      wordScore.initializeHists(wordCounts);
      topicScore.initializeHists(ndMax);
    }
  }

  public void printState(InstanceList docs, int[][] z, String file) {

    try {

      PrintWriter pw = new PrintWriter(file, "UTF-8");

      pw.println("#doc pos typeindex type topic");

      for (int d=0; d<D; d++) {

        FeatureSequence fs = (FeatureSequence) docs.get(d).getData();

        int nd = fs.getLength();

        for (int i=0; i<nd; i++) {

          int w = fs.getIndexAtPosition(i);

          pw.print(d); pw.print(" ");
          pw.print(i); pw.print(" ");
          pw.print(w); pw.print(" ");
          pw.print(docs.getDataAlphabet().lookupObject(w)); pw.print(" ");
          pw.print(z[d][i]); pw.println();
        }
      }

      pw.close();
    }
    catch (IOException e) {
      System.out.println(e);
    }
  }

  // estimate topics

  public void estimate(InstanceList docs, int T, double alpha, double beta, int numItns, int printInterval, int saveSampleInterval, boolean[] symmetric, boolean[] optimize, String documentTopicsFile, String topicWordsFile, String stateFile, String alphaFile, String betaFile, String logProbFile, String hyperparamFile) {

    Alphabet wordDict = docs.getDataAlphabet();

    assert (saveSampleInterval == 0) || (numItns % saveSampleInterval == 0);

    rng = new LogRandoms();

    this.T = T;

    W = wordDict.size();
    D = docs.size();

    System.out.println("Num docs: " + D);
    System.out.println("Num words in vocab: " + W);
    System.out.println("Num topics: " + T);

    wordScore = new WordScoreOpt(W, T, beta);
    topicScore = new TopicScoreOpt(T, D, alpha);

    z = new int[D][];

    long start = System.currentTimeMillis();

    sampleTopics(docs, true);

    try {

      PrintWriter logProbWriter = new PrintWriter(logProbFile, "UTF-8");
      PrintWriter hyperparamWriter = new PrintWriter(hyperparamFile, "UTF-8");

      // count matrices have been populated, every token has been
      // assigned to a single topic, so Gibbs sampling can start

      for (int s=1; s<=numItns; s++) {

        if (s % 10 == 0)
          System.out.print(s);
        else
          System.out.print(".");

        System.out.flush();

        sampleTopics(docs, false);

        if (optimize[0]) {
          if (symmetric[0])
            topicScore.optimizeParamSum(5);
          else
            topicScore.optimizeParam(5);
        }

        if (optimize[1]) {
          if (symmetric[1])
            wordScore.optimizeParamSum(5);
          else
            wordScore.optimizeParam(5);
        }

        if (printInterval != 0) {
          if (s % printInterval == 0) {
            System.out.println();
            wordScore.print(wordDict, 0.0, 10, null);

            double lp = wordScore.logProb(docs, z);

            logProbWriter.println(lp + " " + logProb(docs));
            logProbWriter.flush();

            if (hyperparamFile != null) {

              hyperparamWriter.print(topicScore.getParamSum() + " ");
              hyperparamWriter.print(wordScore.getParamSum() + " ");

              hyperparamWriter.println(); hyperparamWriter.flush();
            }
          }
        }

        if (saveSampleInterval != 0) {
          if (s % saveSampleInterval == 0) {

            printState(docs, z, stateFile + "." + s);

            topicScore.printParam(alphaFile + "." + s);
            wordScore.printParam(betaFile + "." + s);
          }
        }
      }

      Timer.printTimingInfo(start, System.currentTimeMillis());

      if (saveSampleInterval != 0) {
        documentTopicsFile = documentTopicsFile + "." + numItns;
        topicWordsFile = topicWordsFile + "." + numItns;
      }

      topicScore.print(docs, 0.0, -1, documentTopicsFile);
      wordScore.print(wordDict, 0.0, -1, topicWordsFile);
    }
    catch (IOException e) {
      System.out.println(e);
    }
  }
}
