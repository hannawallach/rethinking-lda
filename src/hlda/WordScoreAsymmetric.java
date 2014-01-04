package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import cc.mallet.types.*;

// this does not handle resetting to training data counts etc.

public class WordScoreAsymmetric extends WordScore {

  // create a score function with zero counts

  public WordScoreAsymmetric(int W, int T, double param, String path) {

    this.W = W;
    this.T = T;

    level = new Level[3];

    level[2] = new UniformCounts(new int[] { W }, 0.0, null, null);
    level[1] = new TopicCounts(new int[] { W }, param, level[2], null);
    level[0] = new TopicDocCounts(new int[] { W, T }, param, level[1], path);
  }
}
