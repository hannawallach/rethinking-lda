package edu.umass.cs.iesl.wallach.hierarchical;

import cc.mallet.util.Maths;
import cc.mallet.util.Randoms;

public class LogRandoms extends Randoms {

  public LogRandoms(int seed) {

    super(seed);
  }

  public LogRandoms() {

    super();
  }

  public int nextDiscreteLogDistSlow(double[] logDist, double logDistSum) {

    double r = Math.log(nextUniform()) + logDistSum;
    double acc = Double.NEGATIVE_INFINITY;

    int m = -1;

    for (int i=0; i<logDist.length; i++) {
      acc = Maths.sumLogProb(acc, logDist[i]);

      if (acc > r) {
        m = i;
        break;
      }
    }

    assert m > -1;

    return m;
  }

  public int nextDiscreteLogDist(double[] logDist) {

    double max = Double.NEGATIVE_INFINITY;

    for (int i=0; i<logDist.length; i++)
      if (logDist[i] > max)
        max = logDist[i];

    double[] dist = new double[logDist.length];
    double distSum = 0.0;

    for (int i=0; i<logDist.length; i++) {
      dist[i] = Math.exp(logDist[i] - max);
      distSum += dist[i];
    }

    return nextDiscrete(dist, distSum);
  }

  public int nextDiscrete(double[] dist, double distSum) {

    double r = nextUniform() * distSum;
    double acc = 0.0;

    int m = -1;

    for (int i=0; i<dist.length; i++) {
      acc += dist[i];

      if (acc > r) {
        m = i;
        break;
      }
    }

    assert m > -1;

    return m;
  }
}
