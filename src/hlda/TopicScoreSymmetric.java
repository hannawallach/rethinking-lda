package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import cc.mallet.types.*;

// this does not handle resetting to training data counts etc.

public class TopicScoreSymmetric extends TopicScore {

  // create a score function with zero counts

  public TopicScoreSymmetric(int T, int D, double param) {

    this.T = T;
    this.D = D;

    level = new Level[2];

    level[1] = new UniformCounts(new int[] { T }, 0.0, null, null);
    level[0] = new TopicDocCounts(new int[] { T, D }, param, level[1], null);
  }
}
