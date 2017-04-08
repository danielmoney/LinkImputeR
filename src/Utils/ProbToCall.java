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

package Utils;

import Utils.SingleGenotype.SingleGenotypeCall;
import Utils.SingleGenotype.SingleGenotypeProbability;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts genotype probabilites to a called genotype
 * @author Daniel Money
 * @version 1.0
 */
public class ProbToCall
{

    /**
     * Default constructor - always calls a genotype, no matter the probability
     * of the most probable genotype
     */
    public ProbToCall()
    {
        this(0.0);
    }
    
    /**
     * Creates an instance that will only call a genotype if the most probable
     * genotype has a probability greater than the minProb
     * @param minProb Minimum probability at which to call a genotype
     */
    public ProbToCall(double minProb)
    {
        this.minProb = minProb;
    }
    
    /**
     * Call a single genotype
     * @param prob Genotype probabilities
     * @return The called genotype
     */
    public byte callSingle(double[] prob)
    {
        double maxP = 0.0;
        int geno = -1;
        for (int k = 0; k < 3; k++)
        {
            if (prob[k] > maxP)
            {
                maxP = prob[k];
                geno = k;
            }
        }
        if (maxP < minProb)
        {
            return (byte) -1;
        }
        else
        {
            return (byte) geno;
        }
    }
    
    /**
     * Call a table of genotypes
     * @param probs Table of genotype probabilities
     * @return Table of called genotypes
     */
    public byte[][] call(double[][][] probs)
    {
        byte[][] ret = new byte[probs.length][];
        for (int i = 0; i < probs.length; i++)
        {
            int il = probs[i].length;
            byte[] r = new byte[il];
            ret[i] = r;
            for (int j = 0; j < il; j++)
            {
                r[j] = callSingle(probs[i][j]);
            }
        }
        return ret;
    }
    
    /**
     * Call a list of genotypes
     * @param probs List og genotype probabilities
     * @return List of called genotypes
     */
    public List<SingleGenotypeCall> call(List<SingleGenotypeProbability> probs)
    {
        return probs.stream().map(p -> new SingleGenotypeCall(p.getSample(), p.getSNP(),
            callSingle(p.getProb()))).collect(Collectors.toCollection(ArrayList::new));
    }
    
    private final double minProb;
}
