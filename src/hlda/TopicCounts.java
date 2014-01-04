package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import gnu.trove.*;
import cc.mallet.types.*;

public class TopicCounts extends Level {

  // observed counts

  private TIntArrayList[] topicCountsTables; // N_{j,k}

  private int[] topicCounts; // N_{j}
  private int topicCountsNorm; // N_{.}

  private int T; // constants

  private LogRandoms rng;

  // create a score function with zero counts

  public TopicCounts(int[] consts, double param, Level parent, String path) {

    super(consts.length, param, parent, path);

    assert parent != null;

    assert consts.length == 1;

    this.T = consts[0];

    // allocate space for counts

    topicCountsTables = new TIntArrayList[T];

    for (int j=0; j<T; j++)
      topicCountsTables[j] = new TIntArrayList();

    topicCounts = new int[T];
    topicCountsNorm = 0;

    rng = new LogRandoms();
  }

  public double getScore(int[] indices) {

    assert indices.length == dim;

    int j = indices[0];

    int nj = topicCounts[j];
    int n = topicCountsNorm;

    double score = parent.getScore(new int[] { j });

    score *= param / (n + param);
    score += nj / (n + param);

    return score;
  }

  public void incrementCounts(int[] indices) {

    assert indices.length == dim;

    int j = indices[0];

    if (path != null) {

      int numTables = topicCountsTables[j].size();

      int table = -1;

      if (path.equals("minimal"))
        table = 0;
      else if (path.equals("maximal"))
        table = numTables;
      else {

        double[] dist = new double[numTables + 1];
        double distSum = 0.0;

        for (int k=0; k<numTables; k++) {
          dist[k] = (double) topicCountsTables[j].get(k);
          distSum += dist[k];
        }

        assert distSum == (double) topicCounts[j];

        dist[numTables] = param * parent.getScore(new int[] { j });

        distSum += dist[numTables];

        table = rng.nextDiscrete(dist, distSum);
      }

      assert table >= 0;

      if (table == numTables) {
        topicCountsTables[j].add(1);
        assert topicCountsTables[j].size() == numTables + 1;
        parent.incrementCounts(new int[] { j });
      }
      else {
        int count = topicCountsTables[j].get(table) + 1;
        topicCountsTables[j].set(table, count);
      }
    }

    topicCounts[j]++;
    topicCountsNorm++;
  }

  public void decrementCounts(int[] indices) {

    assert indices.length == dim;

    int j = indices[0];

    if (path != null) {

      int numTables = topicCountsTables[j].size();

      double[] dist = new double[numTables];
      double distSum = 0.0;

      for (int k=0; k<numTables; k++) {
        dist[k] = (double) topicCountsTables[j].get(k);
        distSum += dist[k];
      }

      assert distSum == topicCounts[j];

      int table = rng.nextDiscrete(dist, distSum);

      if (topicCountsTables[j].get(table) == 1) {
        topicCountsTables[j].remove(table);
        assert topicCountsTables[j].size() == numTables - 1;
        parent.decrementCounts(new int[] { j });
      }
      else {
        int count = topicCountsTables[j].get(table) - 1;
        topicCountsTables[j].set(table, count);
      }
    }

    topicCounts[j]--;
    topicCountsNorm--;
  }

  public void resetCounts() {

    for (int j=0; j<T; j++)
      topicCountsTables[j].clear();

    Arrays.fill(topicCounts, 0);
    topicCountsNorm = 0;

    if (path != null)
      parent.resetCounts();
  }
}
