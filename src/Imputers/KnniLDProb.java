/*
 * This file is part of LinkImputeR.
 * 
 * LinkImputeR is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LinkImputeR is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LinkImpute.  If not, see <http://www.gnu.org/licenses/>.
 */

package Imputers;

import Exceptions.ProgrammerException;
import Utils.Correlation.Correlation;
import Utils.Correlation.Pearson;
import Utils.Matrix;
import Utils.ProbToCallMinDepth;
import Utils.Progress.Progress;
import Utils.Progress.ProgressFactory;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypePosition;
import Utils.SingleGenotype.SingleGenotypeProbability;
import Utils.SortByIndexDouble;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Class to perform standard LD-kNNi imputation using probabilities
 * @author Daniel Money
 * @version 0.9
 */
public class KnniLDProb implements Imputer
{
    /**
     * Creates an object to perform LD-kNNi with given values of k and l.
     * @param k The value of k to be used
     * @param l The value of l to be used
     * @param knownDepth At depths at or above this no imputation is done and
     * the imputed probability is the same as the called probability
     */
    public KnniLDProb(int k, int l, int knownDepth)
    {
        this.k = k;
        this.l = l;
        this.knownDepth = knownDepth;
    }
    
    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public KnniLDProb(HierarchicalConfiguration<ImmutableNode> params)
    {
        k = params.getInt("k");
        l = params.getInt("l");
        knownDepth = params.getInt("knowndepth");
    }
    
    public double[][][] impute(double[][][] callprobs, int[][][] readCounts)
    {
        ProbToCallMinDepth p2c = new ProbToCallMinDepth(knownDepth);
        
        byte[][] original = p2c.call(callprobs, readCounts);
        
        Correlation corr = new Pearson();        
       
        byte[][] transposed = Matrix.transpose(original);
        
        //Map<Integer,List<Integer>> ld = corr.topn(transposed, 100);
        Map<Integer,int[]> ld = corr.topn(transposed, 100);
        
        //Integer[][] sim = new Integer[original[0].length][];
        int[][] sim = new int[original[0].length][];
        //for (Entry<Integer,List<Integer>> e: ld.entrySet())
        for (Entry<Integer,int[]> e: ld.entrySet())
        {
            //Integer[] a = new Integer[e.getValue().size()];
            //sim[e.getKey()] = e.getValue().toArray(a);
            sim[e.getKey()] = e.getValue();
        }
        
        double[][][] probs = new double[original.length][][];
        
        Progress progress = ProgressFactory.get(original.length);
        
        IntStream.range(0, original.length).parallel().forEach(i ->
            {
                double[][] p = new double[original[i].length][];
                probs[i] = p;
                IntStream.range(0,original[i].length).forEach(j -> { 
                        if (Arrays.stream(readCounts[i][j]).sum() < knownDepth)
                        {
                            p[j] = imputeSingle(original, i , j, false, sim);
                        }
                        else
                        {
                            p[j] = callprobs[i][j];
                        }
                });
                progress.done();
            }
        );
        
        return probs;
    }
    
    public List<SingleGenotypeProbability> impute(double[][][] callprobs, int[][][] readCounts, List<SingleGenotypeProbability> maskedprobs, List<SingleGenotypeMasked> list)
    {
        ProbToCallMinDepth p2c = new ProbToCallMinDepth(knownDepth);
        
        byte[][] original = p2c.call(callprobs, readCounts);
        
        Correlation corr = new Pearson();
        
        Set<Integer> ldcalc = list.stream().map(sgp -> sgp.getSNP()).collect(Collectors.toCollection(HashSet::new));
        
        byte[][] transposed = Matrix.transpose(original);
        
        Map<Integer,int[]> ld;
        if (ldcalc.size() < (transposed.length / 2))
        {
            ld = corr.limitedtopn(transposed, 100, ldcalc);
        }
        else
        {
            ld = corr.topn(transposed, 100);
        }
        
        int[][] sim = new int[original[0].length][];
        for (Entry<Integer,int[]> e: ld.entrySet())
        {
            sim[e.getKey()] = e.getValue();
        }
        return impute(original, maskedprobs, list, sim);
    }
     
    /**
     * Performs imputation using an already calculated similarity matrix. Used
     * for optimizing parameters as the similarity matrix only has to be calculated
     * once.
     * 
     * The author is aware this description is a bit light on detail.  Please
     * contact the author if further details are needed.
     * @param original Genotypes called based purely on read counts
     * @param callprobs Called genotype probabilities
     * @param list List of masked genotypes and their masked genotype
     * @param sim Similarity matrix
     * @return List of imputed probabilities
     */
    protected List<SingleGenotypeProbability> impute(byte[][] original, List<SingleGenotypeProbability> callprobs, List<SingleGenotypeMasked> list, int[][] sim)
    {
        if (!SingleGenotypePosition.samePositions(callprobs, list))
        {
            //Needs a proper error
            throw new ProgrammerException();
        }
        Progress progress = ProgressFactory.get(list.size());
        return IntStream.range(0, list.size()).mapToObj(i -> 
        {
            SingleGenotypeMasked sgr = list.get(i);
            SingleGenotypeProbability sgc = callprobs.get(i);
            SingleGenotypeProbability sgp;
            if (Arrays.stream(sgr.getMasked()).sum() < knownDepth)
            {
                sgp = new SingleGenotypeProbability(
                    sgr.getSample(), sgr.getSNP(), imputeSingle(original, sgr.getSample(), sgr.getSNP(), true, sim));
            }
            else
            {
                sgp = new SingleGenotypeProbability(
                    sgr.getSample(), sgr.getSNP(), sgc.getProb());
            }
            progress.done();
            return sgp;
        }).collect(Collectors.toCollection(ArrayList::new));
    }
    
    private double[] imputeSingle(byte[][] original, int s, int p, boolean always, int[][] sim)
    {
        if (always || (original[s][p] == -1))
        {                        
            //Calculate the distance to other samples for this snp / sample combination
            double[] dist = dist(s,p,original,sim);

            //Order the samples by their distance
            SortByIndexDouble si = new SortByIndexDouble(dist);
            Integer[] indicies = si.sort();

            int f = 0;
            int i = 0;

            // Store the weights applicable to each of the three genotypes
            double[] neighWeight = new double[3];
            //Loop around samples in order of distance
            do
            {
                // Only impute from samples that have a genotype for the crrent SNP
                if (original[indicies[i]][p] >= 0)
                {
                    neighWeight[original[indicies[i]][p]] += 1.0 / dist[indicies[i]];
                    f++;
                }
                i++;
            }
            // While we haven't seen enough known genotypes and there's still samples left
            while ((f < k) && (i < indicies.length));

            double totalNeighWeight = neighWeight[0] + neighWeight[1] + neighWeight[2];
            double[] neighProb = new double[3];
            neighProb[0] = neighWeight[0] / totalNeighWeight;
            neighProb[1] = neighWeight[1] / totalNeighWeight;
            neighProb[2] = neighWeight[2] / totalNeighWeight;

            return neighProb;
        }
        else
        {
            double[] ret = new double[3];
            ret[original[s][p]] = 1.0;
            return ret;
        }
    }
    
    private double[] dist(int s, int p, byte[][] values, int[][] sim)
    {
        //Simply loops round the other samples, catching the case where it's
        //the current sample
        double[] ret = new double[values.length];
        for (int i = 0; i < values.length; i++)
        {
            if (i != s)
            {
                ret[i] = sdist(values[s], values[i], sim[p]);
            }
            else
            {
                ret[i] = Double.MAX_VALUE;
            }
        }
        return ret;
    }
    
    private double sdist(byte[] v1, byte[] v2, int[] s)
    {
        int d = 0;
        int c = 0;
        // Use the l most similar ones to calculate the distance
        for (int j = 0; j < l; j++)
        {
            int i = s[j];
            int p1 = v1[i];
            int p2 = v2[i];
            if ((p1 != -1) && (p2 != -1))
            {
                // c counts how many snps we've actually used to scale the
                // distance with since some snps will be unknown
                c++;
                d += Math.abs(p1 - p2);
            }
        }
        // If across the l most similar snps there wasn't a single case
        // where both samples had a known genotype then set the distance to
        // max
        if (c == 0)
        {
            return Double.MAX_VALUE - 1;
        }
        //Else return the scaled distance (adding one so we don't have a
        //distance of zero as that caused problems later.
        else
        {
            return ((double) d * (double) l / (double) c) + 1.0;
        }      
    }
    
    public ImmutableNode getConfig()
    {
        ImmutableNode Ik = new ImmutableNode.Builder().name("k").value(k).create();
        ImmutableNode Il = new ImmutableNode.Builder().name("l").value(l).create();
        ImmutableNode Iknowndepth = new ImmutableNode.Builder().name("knowndepth").value(knownDepth).create();
        
        ImmutableNode config = new ImmutableNode.Builder().name("imputation")
                .addChild(Ik)
                .addChild(Il)
                .addChild(Iknowndepth)
                .addAttribute("name", "KnniLD")
                .create();
        
        return config;
    }
    
    private final int knownDepth;
    private final int k;
    private final int l;
}
