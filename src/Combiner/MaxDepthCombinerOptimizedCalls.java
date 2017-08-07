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
import Utils.SingleGenotype.SingleGenotypeMasked;
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
     * @param method The accuracy method to be used
     */
    public MaxDepthCombinerOptimizedCalls(int maxDepth, AccuracyMethod method)
    {
        this.maxDepth = maxDepth;
        this.method = method;
    }

    /**
     * Creates the optimizer from the given configuration
     * @param params The configuration
     */
    public MaxDepthCombinerOptimizedCalls(HierarchicalConfiguration<ImmutableNode> params)
    {
        maxDepth = params.getInt("maxdepth");
        switch(params.getString("method","correct").toLowerCase())
        {
            case "correct":
                method = AccuracyMethod.CORRECT;
                break;
            case "correlation":
                method = AccuracyMethod.CORRELATION;
                break;
        }
    }
    
    public MaxDepthCombiner getOptimized(List<SingleGenotypeProbability> called,
            List<SingleGenotypeProbability> imputed,
            List<SingleGenotypeReads> reads,
            List<SingleGenotypeCall> correct,
            List<SingleGenotypeMasked> masked)
    {
        Opt sco = new Opt(called,imputed,reads,correct,masked,maxDepth,method);
        
        MultipleTest mt = new MultipleTest(0.01);
        double w = mt.optimize(sco, 0.0, 1.0);
        
        return new MaxDepthCombiner(w,maxDepth);
    }
    
    public ImmutableNode getConfig()
    {        
        ImmutableNode Imaxdepth = new ImmutableNode.Builder().name("maxdepth").value(maxDepth).create();
        
        String m;
        switch (method)
        {
            case CORRELATION:
                m = "correlation";
                break;
            default:
                m = "correct";
                break;
        }
        
        ImmutableNode Imethod = new ImmutableNode.Builder().name("method").value(m).create();
        
        ImmutableNode config = new ImmutableNode.Builder().name("combiner")
                .addChild(Imaxdepth)
                .addChild(Imethod)
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
                List<SingleGenotypeCall> correct,
                List<SingleGenotypeMasked> masked,
                int maxDepth,
                AccuracyMethod method)
        {
            this.called = called;
            this.imputed = imputed;
            this.reads = reads;
            this.correct = correct;
            this.masked = masked;
            
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
                case CORRELATION:
                    return AccuracyCalculator.correlation(correct,resultsCall,masked);
                case CORRECT:
                default:
                    return AccuracyCalculator.accuracy(correct,resultsCall);
            }
        }    
    
        private AccuracyMethod method;
        private int maxDepth;
        private ProbToCall p2c;
        private List<SingleGenotypeMasked> masked;
        private List<SingleGenotypeCall> correct;
        private List<SingleGenotypeProbability> called;
        private List<SingleGenotypeProbability> imputed;
        private List<SingleGenotypeReads> reads;
    }
}
