package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.*;
import cc.mallet.types.*;

// this does not handle resetting to training data counts etc.

public abstract class HDScore {

  public abstract double[] getParam();
  public abstract void setParam(double[] param);

  public abstract double logProb(InstanceList docs, int[][] z);

  public void printParam(String file) {

    double[] param = getParam();

    try {

      PrintWriter pw = new PrintWriter(file, "UTF-8");

      for (int i=0; i<param.length; i++)
        pw.println(param[i]);

      pw.close();
    }
    catch (IOException e) {
      System.out.println(e);
    }
  }

  // there's a lot of cloning going on in the following method, much
  // of which may not be necessary...

  private double logProb(InstanceList docs, int[][] z, double[] logParam) {

    // clone the old parameter values

    double[] oldParam = getParam().clone();

    // make a new parameter value array

    double[] param = new double[oldParam.length];

    for (int i=0; i<param.length; i++)
      param[i] = Math.exp(logParam[i]);

    // set the parameters and compute log probability

    setParam(param);

    double logProb = logProb(docs, z);

    // reset the parameters

    setParam(oldParam.clone());

    return logProb;
  }

  // code to sample parameter values

  public void sampleParam(InstanceList docs, int[][] z, LogRandoms rng, int numItns, double stepSize) {

    double[] param = getParam();

    int I = param.length;

    double[] rawParam = new double[I];
    double rawParamSum = 0.0;

    for (int i=0; i<I; i++) {
      rawParam[i] = Math.log(param[i]);
      rawParamSum += rawParam[i];
    }

    double[] l = new double[I];
    double[] r = new double[I];

    for (int s=0; s<numItns; s++) {

      double lp = logProb(docs, z, rawParam) + rawParamSum;
      double lpNew = Math.log(rng.nextUniform()) + lp;

      for (int i=0; i<I; i++) {
        l[i] = rawParam[i] - rng.nextUniform() * stepSize;
        r[i] = l[i] + stepSize;
      }

      double[] rawParamNew = new double[I];
      double rawParamNewSum = 0.0;

      while (true) {

        for (int i=0; i<I; i++) {
          rawParamNew[i] = l[i] + rng.nextUniform() * (r[i] - l[i]);
          rawParamNewSum += rawParamNew[i];
        }

        if ((logProb(docs, z, rawParamNew) + rawParamNewSum) > lpNew)
          break;
        else
          for (int i=0; i<I; i++)
            if (rawParamNew[i] < rawParam[i])
              l[i] = rawParamNew[i];
            else
              r[i] = rawParamNew[i];
      }

      rawParam = rawParamNew;
      rawParamSum = rawParamNewSum;
    }

    for (int i=0; i<I; i++)
      param[i] = Math.exp(rawParam[i]);

    setParam(param);
  }
}
