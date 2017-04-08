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

package Combiner;

import Utils.Progress.Progress;
import Utils.Progress.ProgressFactory;
import Utils.SingleGenotype.SingleGenotypePosition;
import Utils.SingleGenotype.SingleGenotypeProbability;
import Utils.SingleGenotype.SingleGenotypeReads;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Combines called and imputed genotype probablities, only using called genotypes
 * above a given read depth
 * @author Daniel Money
 * @version 0.9
 */
public class MaxDepthCombiner implements Combiner
{

    /**
     * Constructor
     * @param w The weight to give to the imputed probabilities
     * @param maxDepth The depth above which only called genotypes are used
     */
    public MaxDepthCombiner(double w, int maxDepth)
    {
        this.w = w;
        this.maxDepth = maxDepth;
    }

    /**
     * Creates the combiner from the given configuration
     * @param params The configuration
     */
    public MaxDepthCombiner(HierarchicalConfiguration<ImmutableNode> params)
    {
        w = params.getDouble("w");
        maxDepth = params.getInt("maxdepth");
    }
    
    public double[][][] combine(double[][][] called, double[][][] imputed, int[][][] reads)
    {
        double[][][] probs = new double[called.length][][];
        
        Progress progress = ProgressFactory.get(called.length);
        
        IntStream.range(0, called.length).parallel().forEach(i ->
            {
                int l = called[i].length;
                double[][] p = new double[l][];
                probs[i] = p;
                IntStream.range(0,l).forEach(j -> p[j] = combineSingle(called[i][j], imputed[i][j], reads[i][j]));
                progress.done();
            }
        );
        
        return probs;        
    }
    
    public List<SingleGenotypeProbability> combine(List<SingleGenotypeProbability> called,
            List<SingleGenotypeProbability> imputed,
            List<SingleGenotypeReads> reads)
    {
        if (!SingleGenotypePosition.samePositions(called, imputed)) 
        {
            //Throw Exception
        }
        
        Progress progress = ProgressFactory.get(called.size());
        return IntStream.range(0, called.size()).parallel().mapToObj(i ->
        {
            SingleGenotypeProbability c = called.get(i);
            SingleGenotypeProbability sgp = new SingleGenotypeProbability(
                c.getSample(), c.getSNP(), 
                combineSingle(c.getProb(), imputed.get(i).getProb(), reads.get(i).getReads()));
            progress.done();
            return sgp;
        }).collect(Collectors.toCollection(ArrayList::new));
    }
    
    private double[] combineSingle(double[] called, double[] imputed, int[] reads)
    {
        if (Arrays.stream(reads).sum() <= maxDepth)
        {
            double[] totalProb = new double[3];

            totalProb[0] = w * imputed[0] + (1.0 - w) * called[0];
            totalProb[1] = w * imputed[1] + (1.0 - w) * called[1];
            totalProb[2] = w * imputed[2] + (1.0 - w) * called[2];

            return totalProb;
        }
        else
        {
            return called;
        }
    }
    
    public ImmutableNode getConfig()
    {
        ImmutableNode Iw = new ImmutableNode.Builder().name("w").value(w).create();
        ImmutableNode Imaxdepth = new ImmutableNode.Builder().name("maxdepth").value(maxDepth).create();
        
        
        ImmutableNode config = new ImmutableNode.Builder().name("combiner")
                .addChild(Iw)
                .addChild(Imaxdepth)
                .addAttribute("name", "MaxDepth")
                .create();
        
        return config;
    }
    
    private final int maxDepth;
    private final double w;
}
