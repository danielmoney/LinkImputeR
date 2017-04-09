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

import Accuracy.AccuracyCalculator;
import Accuracy.AccuracyCalculator.AccuracyMethod;
import Utils.Correlation.Correlation;
import Utils.Correlation.Pearson;
import Utils.Matrix;
import Utils.Optimize.Descent;
import Utils.Optimize.MultipleIntegerValue;
import Utils.ProbToCall;
import Utils.ProbToCallMinDepth;
import Utils.SingleGenotype.SingleGenotypeCall;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypeProbability;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Represents a KnniLDProb imputer that can be optimized to mazimise the number
 * of correctly called genotyped
 * @author Daniel Money
 * @version 0.9
 */
public class KnniLDProbOptimizedCalls implements OptimizeImputer<KnniLDProb>
{

    /**
     * Constructor
     * @param knownDepth The read depth above which imputation is not performed
     * and the called probabilities are used instead
     */
    public KnniLDProbOptimizedCalls(int knownDepth)
    {
        this.knownDepth = knownDepth;
        this.method = AccuracyMethod.Accuracy;
        ///HERE
    }
    
    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public KnniLDProbOptimizedCalls(HierarchicalConfiguration<ImmutableNode> params)
    {
        knownDepth = params.getInt("knowndepth");
        this.method = AccuracyMethod.Accuracy;
        ///HERE
    }
    
    
    public KnniLDProb getOptimized(double[][][] callprobs, int[][][] readCounts, List<SingleGenotypeProbability> maskedprobs, List<SingleGenotypeMasked> list) throws Exception
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
        
        List<SingleGenotypeCall> correct = list.stream().map(sgp ->
                new SingleGenotypeCall(sgp.getSample(),sgp.getSNP(),original[sgp.getSample()][sgp.getSNP()]))
            .collect(Collectors.toCollection(ArrayList::new));
        
        int[][] sim = new int[original[0].length][];
        for (Map.Entry<Integer,int[]> e: ld.entrySet())
        {
            sim[e.getKey()] = e.getValue();
        }
        
        Opt opt = new Opt(original,sim,maskedprobs,list,correct,method);
        
        int[] min = {1,1};
        int[] max = {original.length,100};
        int[] init = {5,20};
        Descent descent = new Descent();
        int[] best = descent.optimize(opt, init, min, max);

        int k = best[0];
        int l = best[1];

        return new KnniLDProb(k,l,knownDepth);
    }
    
    public ImmutableNode getConfig()
    {        
        ImmutableNode Iknowndepth = new ImmutableNode.Builder().name("knowndepth").value(knownDepth).create();
        
        ImmutableNode config = new ImmutableNode.Builder().name("imputation")
                .addAttribute("name", "KnniLDOpt")
                .addChild(Iknowndepth)
                .create();
        
        return config;
    }
    
    private AccuracyMethod method;
    private final int knownDepth;
    
    private class Opt implements MultipleIntegerValue
    {
        public Opt(byte[][] original, int[][] sim,
                List<SingleGenotypeProbability> maskedprobs,
                List<SingleGenotypeMasked> list,
                List<SingleGenotypeCall> correct,
                AccuracyMethod method)
        {
            this.original = original;
            this.sim = sim;
            this.maskedprobs = maskedprobs;
            this.list = list;
            this.correct = correct;
            this.p2c = new ProbToCall();
        }

        public double value(int[] params)
        {
            KnniLDProb knni = new KnniLDProb(params[0],params[1],knownDepth);
            List<SingleGenotypeProbability> resultsProb = knni.impute(original,maskedprobs,list,sim);
            List<SingleGenotypeCall> resultsCall = p2c.call(resultsProb);
            switch (method)
            {
                case Correlation:
                    return AccuracyCalculator.correlation(correct,resultsCall);
                case Accuracy:
                default:
                    return AccuracyCalculator.accuracy(correct,resultsCall);
            }
        }    
        
        private byte[][] original;
        private int[][] sim;
        private ProbToCall p2c;
        private List<SingleGenotypeProbability> maskedprobs;
        private List<SingleGenotypeMasked> list;
        private List<SingleGenotypeCall> correct;
    }
}
