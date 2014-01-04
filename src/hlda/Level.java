package edu.umass.cs.iesl.wallach.hierarchical;

public abstract class Level {

  protected int dim; // dimensionality of count matrix for this level

  protected double param; // hyperparameter for this level

  protected Level parent; // parent level

  protected String path; // path assumption (minimal, maximal, gibbs)

  public Level(int dim, double param, Level parent, String path) {

    this.dim = dim;
    this.param = param;
    this.parent = parent;
    this.path = path;
  }

  public abstract double getScore(int[] indices);
  public abstract void incrementCounts(int[] indices);
  public abstract void decrementCounts(int[] indices);
  public abstract void resetCounts();

  public double getParam() {

    return param;
  }

  public void setParam(double param) {

    this.param = param;
  }
}
