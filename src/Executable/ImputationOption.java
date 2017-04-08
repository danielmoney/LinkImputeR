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

import Exceptions.ProgrammerException;
import Imputers.Imputer;
import Imputers.KnniLDProb;
import Imputers.KnniLDProbOptimizedCalls;
import Imputers.OptimizeImputer;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypeProbability;
import java.util.List;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Reperesnts the imputer options to LinkImpute
 * @author daniel
 */
public class ImputationOption
{
    /**
     * Constructor for when the imputer isn't optimizable.  May be used
     * when we've already created an optimized imputer.
     * @param imputer The imputer
     */
    public ImputationOption(Imputer imputer)
    {
        this.imputer = imputer;
        this.opt = null;
    }
    
    /**
     * Constructor for when the imputer is optimizable
     * @param imputer The imputer
     */
    public ImputationOption(OptimizeImputer imputer)
    {
        this.imputer = null;
        this.opt = imputer;
    }

    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public ImputationOption(HierarchicalConfiguration<ImmutableNode> params)
    {
        String method  = params.getString("[@name]");
        switch (method)
        {
            case "KnniLD":
                imputer = new KnniLDProb(params);
                break;
            case "KnniLDOpt":
                opt = new KnniLDProbOptimizedCalls(params);
                break;
            default:
                //NEEDS A  PROPER EXCEPTION!
                throw new ProgrammerException();
        }
    }
 
    /**
     * Gets an imputer.  If the imputer option was for one that was not optiized
     * simply returns that.  Elses optimzes a imputer and returns the optimized
     * version of it.
     * @param original The original called genotype probabilities
     * @param readCounts The readcounts
     * @param maskedprobs The masked called genotype probabilities
     * @param list A list of masked positions
     * @return An imputer
     * @throws Exception If there is a problem
     */
    public Imputer getImputer(
            double[][][] original, int[][][] readCounts,
            List<SingleGenotypeProbability> maskedprobs,
            List<SingleGenotypeMasked> list) throws Exception
    {
        if (imputer != null)
        {
            return imputer;
        }
        else
        {
            return opt.getOptimized(original, readCounts, maskedprobs, list);
        }
    }

    /**
     * Returns the imputer if the imputer option was for a non-optimizable
     * imputer, else throws an exception
     * @return The imputer
     */
    public Imputer getImputer()
    {
        if (imputer != null)
        {
            return imputer;
        }
        else
        {
            //NEEDS PROPER EXCEPTION
            throw new ProgrammerException();  
        }
    }
        
    /**
     * Get the imputer options config
     * @return The config
     */       
    public ImmutableNode getConfig()
    {
        if (imputer != null)
        {
            return imputer.getConfig();
        }
        else
        {
            return opt.getConfig();
        }
    }
    
    private Imputer imputer;
    private OptimizeImputer<?> opt;
}
