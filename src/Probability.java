package edu.umass.cs.iesl.wallach.hierarchical;

public class Probability implements Comparable {

  public int index;
  public double prob;

  public Probability(int index, double prob) {
    this.index = index;
    this.prob = prob;
  }

  public final int compareTo(Object o) {
    if (prob > ((Probability) o).prob)
      return -1;
    else if (prob == ((Probability) o).prob)
      return 0;
    else
      return 1;
  }
}
