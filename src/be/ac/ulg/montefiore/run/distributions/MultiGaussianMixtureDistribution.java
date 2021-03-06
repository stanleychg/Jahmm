/* jahmm package - v0.6.1 */

/*
  *  Copyright (c) 2004-2006, Jean-Marc Francois.
  *  Copyright (c) 2013, Aubry Cholleton : 
  *  Adaptation of GaussianMixtureDistribution to Multivariate Gaussians
 *
 *  This file is part of Jahmm.
 *  Jahmm is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Jahmm is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jahmm; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */

package be.ac.ulg.montefiore.run.distributions;

import java.util.*;


/**
 * This class implements a Gaussian mixture distribution.
 *
 * @author Jean-Marc Francois (based on code from Benjamin Chung)
 */
public class MultiGaussianMixtureDistribution
implements MultiRandomDistribution
{ 
  static private final Random random = new Random();
  
  private MultiGaussianDistribution[] distributions;
  private double proportions[];
  
  
  /**
   * Creates a new pseudo-random, Gaussian mixture distribution.  It is
   * made of Gaussian distributions evently distributed between 0 and 1 with
   * a unitary variance.
   *
   * @param nbGaussians The number of distributions composing the mixture.
   */
  public MultiGaussianMixtureDistribution(int nbGaussians, int dimension)
  {
    if (nbGaussians <= 0)
      throw new IllegalArgumentException("Argument must be strictly " +
          "positive");
    
    distributions = new MultiGaussianDistribution[nbGaussians];
    proportions = new double[nbGaussians];
    double means[][] = new double[nbGaussians][dimension];
    double covariances[][][] = new double[nbGaussians][dimension][dimension];
    
    for (int i = 0; i < nbGaussians; i++) {
      for (int j = 0; j < dimension; j++) {
        // TODO Adapt to the multivariate case if needed
        means[i][j] = (1. + 2. * (double) i) / (2. * (double) nbGaussians);
        
      }
    }
    for (double[][] matrix : covariances) {
      for (int i = 0; i < dimension; i++)
        matrix[i][i] = 1.;
    }
    Arrays.fill(proportions, 1. / ((double) nbGaussians));
    
    for (int i = 0; i < distributions.length; i++)
      distributions[i] = new MultiGaussianDistribution(means[i], covariances[i]);
  }
  
  
  /**
   * Creates a new pseudo-random, Gaussian mixture distribution.  The mean
   * values, variances and proportions of each distribution is given as
   * an argument.
   *
   * @param means The mean values of the Gaussian distributions.
   * @param variances The variances of the Gaussian distributions.
   * @param proportions The mixing proportions. This array does not have to
   *             be normalized, but each element must be positive and the sum
   *             of its elements must be strictly positive.
   */
  public MultiGaussianMixtureDistribution(double[][] means, double[][][] covariances,
      double[] proportions)
  {
    if (means.length == 0 || means.length != covariances.length ||
        means.length != proportions.length ||
        means[0].length != covariances[0].length ||
        covariances[0].length != covariances[0][0].length)
      throw new IllegalArgumentException();
    
    distributions = new MultiGaussianDistribution[means.length];
    this.proportions = new double[means.length];
    
    for (int i = 0; i < distributions.length; i++)
      distributions[i] = new MultiGaussianDistribution(means[i], covariances[i]);
    
    double sum = 0.;
    for (int i = 0; i < proportions.length; i++)
      sum += proportions[i];
    
    for (int i = 0; i < proportions.length; i++)
      this.proportions[i] = proportions[i] / sum;
  }
  
  
  /**
   * Returns the number of Gaussians composing this mixture.
   *
   * @return The number of Gaussians composing this mixture.
   */
  public int nbGaussians()
  {
    return distributions.length;
  }
  
  public int dimension()
  {
    return distributions[0].dimension();
  }
  
  /**
   * Returns the distributions composing this mixture.
   *
   * @return A copy of the distributions array.
   */
  public MultiGaussianDistribution[] distributions()
  {
    return distributions.clone();
  }
  
  
  /**
   * Returns the proportions of the distributions in this mixture.
   * The sum of the proportions equals 1.
   *
   * @return A copy of the distributions' proportions array.
   */
  public double[] proportions() 
  {
    return proportions.clone();
  }
  
  
  public double[] generate() 
  {
    double r = random.nextDouble();
    double sum = 0.;  
    
    for (int i = 0; i < proportions.length; i++) {
      sum += proportions[i];
      
      if (r <= sum)
        return distributions[i].generate();
    }
    
    throw new RuntimeException("Internal error");
  }
  
  
  public double probability(double[] v)
  {
    double sum = 0.;
    
    for (int i = 0; i < distributions.length; i++)
      sum += distributions[i].probability(v) * proportions[i];
    
    return sum;
  }
  
  
  private static final long serialVersionUID = 2634624658500627330L;
}