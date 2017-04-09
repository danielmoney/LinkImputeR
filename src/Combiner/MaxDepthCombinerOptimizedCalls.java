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

import Accuracy.AccuracyCalculator;
import Accuracy.AccuracyCalculator.AccuracyMethod;
import Utils.Optimize.MultipleTest;
import Utils.Optimize.SingleDoubleValue;
import Utils.ProbToCall;
import Utils.SingleGenotype.SingleGenotypeCall;
import Utils.SingleGenotype.SingleGenotypeProbability;
import Utils.SingleGenotype.SingleGenotypeReads;
import java.util.List;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Gets an optimized MaxDepthCombiner.  That is it is used to get a
 * MaxDepthCombiner with optimized w.
 * @author daniel
 */
public class MaxDepthCombinerOptimizedCalls implements OptimizeCombiner<MaxDepthCombiner>
{

    /**
     * Constructor
     * @param maxDepth The depth above which only called genotype probabilities are used
     */
    public MaxDepthCombinerOptimizedCalls(int maxDepth)
    {
        this.maxDepth = maxDepth;
        this.method = AccuracyMethod.Accuracy;
        ///HERE
    }

    /**
     * Creates the optimizer from the given configuration
     * @param params The configuration
     */
    public MaxDepthCombinerOptimizedCalls(HierarchicalConfiguration<ImmutableNode> params)
    {
        maxDepth = params.getInt("maxdepth");
        this.method = AccuracyMethod.Accuracy;
        ///HERE
    }
    
    public MaxDepthCombiner getOptimized(List<SingleGenotypeProbability> called,
            List<SingleGenotypeProbability> imputed,
            List<SingleGenotypeReads> reads,
            List<SingleGenotypeCall> correct) throws Exception
    {
        Opt sco = new Opt(called,imputed,reads,correct,maxDepth,method);
        
        MultipleTest mt = new MultipleTest(0.01);
        double w = mt.optimize(sco, 0.0, 1.0);
        
        return new MaxDepthCombiner(w,maxDepth);
    }
    
    public ImmutableNode getConfig()
    {        
        ImmutableNode Imaxdepth = new ImmutableNode.Builder().name("maxdepth").value(maxDepth).create();
        
        ImmutableNode config = new ImmutableNode.Builder().name("combiner")
                .addChild(Imaxdepth)
                .addAttribute("name", "MaxDepthOpt")
                .create();
        
        return config;
    }

    private AccuracyMethod method;
    private int maxDepth;
    
    private class Opt implements SingleDoubleValue
    {
        public Opt(List<SingleGenotypeProbability> called,
                List<SingleGenotypeProbability> imputed,
                List<SingleGenotypeReads> reads, 
                List<SingleGenotypeCall> correct, int maxDepth,
                AccuracyMethod method)
        {
            this.called = called;
            this.imputed = imputed;
            this.reads = reads;
            this.correct = correct;
            
            this.maxDepth = maxDepth;
            
            this.p2c = new ProbToCall();

            this.method = method;
        }

        public double value(double param)
        {
            MaxDepthCombiner combiner = new MaxDepthCombiner(param,maxDepth);
            List<SingleGenotypeProbability> resultsProb = combiner.combine(called,imputed,reads);
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
    
        private AccuracyMethod method;
        private int maxDepth;
        private ProbToCall p2c;
        private List<SingleGenotypeCall> correct;
        private List<SingleGenotypeProbability> called;
        private List<SingleGenotypeProbability> imputed;
        private List<SingleGenotypeReads> reads;
    }
}
