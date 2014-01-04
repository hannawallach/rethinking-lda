package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import cc.mallet.types.*;

// this does not handle resetting to training data counts etc.

public class TopicScoreAsymmetric extends TopicScore {

  // create a score function with zero counts

  public TopicScoreAsymmetric(int T, int D, double param, String path) {

    this.T = T;
    this.D = D;

    level = new Level[3];

    level[2] = new UniformCounts(new int[] { T }, 0.0, null, null);
    level[1] = new TopicCounts(new int[] { T }, param, level[2], null);
    level[0] = new TopicDocCounts(new int[] { T, D }, param, level[1], path);
  }
}
