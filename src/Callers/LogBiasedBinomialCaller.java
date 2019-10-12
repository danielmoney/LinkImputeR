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
public class LogBiasedBinomialCaller extends Caller
{

    /**
     * Creates a biased caller with the given error and bias
     * @param error The error rate
     * @param bias The bias - given as the probability of the allele coded 0
     */
    public LogBiasedBinomialCaller(double error, double bias)
    {
        this.error = error;
        this.bias = bias;

        logerror = Math.log(error);
        log1merror = Math.log1p(-error);
        logbias = Math.log(bias);
        log1mbias = Math.log(1.0-bias);
    }

    /**
     * Creates the caller from the given configuration
     * @param params The configuration
     */
    public LogBiasedBinomialCaller(HierarchicalConfiguration<ImmutableNode> params)
    {
        error = params.getInt("error");
        bias = params.getInt("bias");

        logerror = Math.log(error);
        log1merror = Math.log1p(-error);
        logbias = Math.log(bias);
        log1mbias = Math.log(1.0-bias);
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

            double ll0 = d[0] * log1merror + d[1] * logerror;
            double ll1 = d[0] * logbias + d[1] * log1mbias;
            double ll2 = d[0] * logerror + d[1] *log1merror;

            double maxll = Math.max(ll0,Math.max(ll1,ll2));

            double l0 = Math.exp(ll0 - maxll);
            double l1 = Math.exp(ll1 - maxll);
            double l2 = Math.exp(ll2 - maxll);

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
                .addAttribute("name", "LogBiasedBinomial")
                .create();
        
        return config;
    }

    /***
     * Get the eror rate used in the caller
     * @return The error rate
     */
    public double getError()
    {
        return error;
    }

    final double error;
    final double bias;

    final double logerror;
    final double log1merror;
    final double logbias;
    final double log1mbias;
}
