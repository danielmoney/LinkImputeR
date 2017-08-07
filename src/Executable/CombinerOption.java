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

package Executable;

import Combiner.Combiner;
import Combiner.MaxDepthCombiner;
import Combiner.MaxDepthCombinerOptimizedCalls;
import Combiner.OptimizeCombiner;
import Exceptions.ProgrammerException;
import Utils.SingleGenotype.SingleGenotypeCall;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypeProbability;
import Utils.SingleGenotype.SingleGenotypeReads;
import java.util.List;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Reperesnts the combiner options to LinkImpute
 * @author daniel
 */
public class CombinerOption
{

    /**
     * Constructor for when the combiner isn't optimizable.  May be used
     * when we've already created an optimized combiner.
     * @param combiner The combiner
     */
    public CombinerOption(Combiner combiner)
    {
        this.combiner = combiner;
        this.opt = null;
    }
    
    /**
     * Constructor for when the combiner is optimizable
     * @param combiner The combiner
     */
    public CombinerOption(OptimizeCombiner combiner)
    {
        this.combiner = null;
        this.opt = combiner;
    }

    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public CombinerOption(HierarchicalConfiguration<ImmutableNode> params)
    {
        String method  = params.getString("[@name]");
        switch (method)
        {
            case "MaxDepth":
                combiner = new MaxDepthCombiner(params);
                break;
            case "MaxDepthOpt":
                opt = new MaxDepthCombinerOptimizedCalls(params);
                break;
            default:
                //NEEDS A  PROPER EXCEPTION!
                throw new ProgrammerException();
        }
    }
    
    /**
     * Gets a combiner.  If the combiner option was for one that was not optiized
     * simply returns that.  Elses optimzes a combiner and returns the optimized
     * version of it.
     * @param called The called genotype probabilities
     * @param imputed The imputed genotype probabilities
     * @param reads The reads
     * @param correct The correct genotypes
     * @return A combiner
     */
    public Combiner getCombiner(
            List<SingleGenotypeProbability> called,
            List<SingleGenotypeProbability> imputed,
            List<SingleGenotypeReads> reads,
            List<SingleGenotypeCall> correct,
            List<SingleGenotypeMasked> masked)
    {
        if (combiner != null)
        {
            return combiner;
        }
        else
        {
            return opt.getOptimized(called, imputed, reads, correct, masked);
        }
    }
    
    /**
     * Returns the combiner if the combiner option was for a non-optimizable
     * combiner, else throws an exception
     * @return The combiner
     */
    public Combiner getCombiner()
    {
        if (combiner != null)
        {
            return combiner;
        }
        else
        {
            //NEEDS PROPER EXCEPTION
            throw new ProgrammerException();  
        }
    }
        
    /**
     * Get the combiner options config
     * @return The config
     */   
    public ImmutableNode getConfig()
    {
        if (combiner != null)
        {
            return combiner.getConfig();
        }
        else
        {
            return opt.getConfig();
        }
    }
    
    private Combiner combiner;
    private OptimizeCombiner<?> opt;
}
