package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import java.util.*;
import gnu.trove.*;
import cc.mallet.types.*;

public class UniformCounts extends Level {

  private int T; // constants

  private LogRandoms rng;

  // create a score function

  public UniformCounts(int[] consts, double param, Level parent, String path) {

    super(consts.length, param, parent, path);

    assert param == 0.0;
    assert parent == null;
    assert path == null;

    assert consts.length == 1;

    this.T = consts[0];
  }

  public double getScore(int[] indices) {

    return 1.0 / T;
  }

  public void incrementCounts(int[] indices) {

    return;
  }

  public void decrementCounts(int[] indices) {

    return;
  }

  public void resetCounts() {

    return;
  }
}
