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

package Callers;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * A biased binomial caller, that is one where each read is not equally likely
 * in the case of a hetrozygote
 * @author Daniel Money
 * @version 1.1.3
 */
public class BiasedBinomialCaller extends Caller
{

    /**
     * Creates a biased caller with the given error and bias
     * @param error The error rate
     * @param bias The bias - given as the probability of the allele coded 0
     */
    public BiasedBinomialCaller(double error, double bias)
    {
        this.error = error;
        this.bias = bias;
    }

    /**
     * Creates the caller from the given configuration
     * @param params The configuration
     */
    public BiasedBinomialCaller(HierarchicalConfiguration<ImmutableNode> params)
    {
        error = params.getInt("error");
        bias = params.getInt("bias");
    }
    
    public double[] callSingle(int[] depths, int i, int j)
    {
        return callSingle(depths);
    }
    
    /**
     * Calls a genotype based on reads
     * @param d The reads
     * @return The called genotype
     */
    public double[] callSingle(int[] d)
    {
        if ((d[0] + d[1]) != 0)
        {
            double[] probs = new double[3];

            double l0 = Math.pow(1-error,d[0]) * Math.pow(error,d[1]);
            double l1 = Math.pow(bias,d[0]) * Math.pow(1-bias,d[1]);
            double l2 = Math.pow(error,d[0]) * Math.pow(1-error,d[1]);

            double totall = l0 + l1 + l2;

            probs[0] = l0 / totall;
            probs[1] = l1 / totall;
            probs[2] = l2 / totall;

            return probs;
        }
        else
        {
            double[] probs = {1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0};
            return probs;
        }
    }

    public ImmutableNode getConfig()
    {
        ImmutableNode Ierror = new ImmutableNode.Builder().name("error").value(error).create();
        ImmutableNode Ibias = new ImmutableNode.Builder().name("bias").value(bias).create(); 
        
        ImmutableNode config = new ImmutableNode.Builder().name("caller")
                .addChild(Ierror)
                .addChild(Ibias)
                .addAttribute("name", "BiasedBinomial")
                .create();
        
        return config;
    }
    
    final double error;
    final double bias;
}
