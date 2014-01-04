package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import gnu.trove.*;
import cc.mallet.types.*;

public class TopicDocCounts extends Level {

  // observed counts

  private TIntArrayList[][] topicDocCountsTables; // N_{j|d,k}

  private int[][] topicDocCounts; // N_{j|d}
  private int[] topicDocCountsNorm; // N_{.|d}

  private int T, D; // constants

  private LogRandoms rng;

  // create a score function with zero counts

  public TopicDocCounts(int[] consts, double param, Level parent, String path) {

    super(consts.length, param, parent, path);

    assert parent != null;

    assert consts.length == 2;

    this.T = consts[0];
    this.D = consts[1];

    // allocate space for counts

    topicDocCountsTables = new TIntArrayList[T][D];

    for (int j=0; j<T; j++)
      for (int d=0; d<D; d++)
        topicDocCountsTables[j][d] = new TIntArrayList();

    topicDocCounts = new int[T][D];
    topicDocCountsNorm = new int[D];

    rng = new LogRandoms();
  }

  public double getScore(int[] indices) {

    assert indices.length == dim;

    int j = indices[0];
    int d = indices[1];

    int njd = topicDocCounts[j][d];
    int nd = topicDocCountsNorm[d];

    double score = parent.getScore(new int[] { j });

    score *= param / (nd + param);
    score += njd / (nd + param);

    return score;
  }

  public void incrementCounts(int[] indices) {

    assert indices.length == dim;

    int j = indices[0];
    int d = indices[1];

    if (path != null) {

      int numTables = topicDocCountsTables[j][d].size();

      int table = -1;

      if (path.equals("minimal"))
        table = 0;
      else if (path.equals("maximal"))
        table = numTables;
      else {

        double[] dist = new double[numTables + 1];
        double distSum = 0.0;

        for (int k=0; k<numTables; k++) {
          dist[k] = (double) topicDocCountsTables[j][d].get(k);
          distSum += dist[k];
        }

        assert distSum == (double) topicDocCounts[j][d];

        dist[numTables] = param * parent.getScore(new int[] { j });
        distSum += dist[numTables];

        table = rng.nextDiscrete(dist, distSum);
      }

      assert table >= 0;

      if (table == numTables) {
        topicDocCountsTables[j][d].add(1);
        assert topicDocCountsTables[j][d].size() == numTables + 1;
        parent.incrementCounts(new int[] { j });
      }
      else {
        int count = topicDocCountsTables[j][d].get(table) + 1;
        topicDocCountsTables[j][d].set(table, count);
      }
    }

    topicDocCounts[j][d]++;
    topicDocCountsNorm[d]++;
  }

  public void decrementCounts(int[] indices) {

    assert indices.length == dim;

    int j = indices[0];
    int d = indices[1];

    if (path != null) {

      int numTables = topicDocCountsTables[j][d].size();

      double[] dist = new double[numTables];
      double distSum = 0.0;

      for (int k=0; k<numTables; k++) {
        dist[k] = (double) topicDocCountsTables[j][d].get(k);
        distSum += dist[k];
      }

      assert distSum == topicDocCounts[j][d];

      int table = rng.nextDiscrete(dist, distSum);

      if (topicDocCountsTables[j][d].get(table) == 1) {
        topicDocCountsTables[j][d].remove(table);
        assert topicDocCountsTables[j][d].size() == numTables - 1;
        parent.decrementCounts(new int[] { j });
      }
      else {
        int count = topicDocCountsTables[j][d].get(table) - 1;
        topicDocCountsTables[j][d].set(table, count);
      }
    }

    topicDocCounts[j][d]--;
    topicDocCountsNorm[d]--;
  }

  public void resetCounts() {

    for (int j=0; j<T; j++)
      for (int d=0; d<D; d++)
        topicDocCountsTables[j][d].clear();

    for (int j=0; j<T; j++)
      Arrays.fill(topicDocCounts[j], 0);

    Arrays.fill(topicDocCountsNorm, 0);

    if (path != null)
      parent.resetCounts();
  }
}
