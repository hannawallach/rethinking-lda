package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import cc.mallet.types.*;

// this does not handle resetting to training data counts etc.

public class WordScoreSymmetric extends WordScore {

  // create a score function with zero counts

  public WordScoreSymmetric(int W, int T, double param) {

    this.W = W;
    this.T = T;

    level = new Level[2];

    level[1] = new UniformCounts(new int[] { W }, 0.0, null, null);
    level[0] = new TopicDocCounts(new int[] { W, T }, param, level[1], null);
  }
}
