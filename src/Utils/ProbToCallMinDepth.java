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

import java.util.stream.IntStream;

/**
 * Converts genotype probabilities to a called genotype
 * @author Daniel Money
 * @version 1.0
 */
public class ProbToCallMinDepth
{
    /**
     * Default constructor - always calls a genotype, no matter the number of
     * reads
     */
    public ProbToCallMinDepth()
    {
        this(0);
    }

    /**
     * Creates an instance that will only call a genotype if there are more than
     * the given number of reads for that genotype
     * @param minDepth Minimum number of reads at which to call a genotype
     */
    public ProbToCallMinDepth(int minDepth)
    {
        this.minDepth = minDepth;
    }

    /**
     * Call a single genotype
     * @param prob Genotype probabilities
     * @param readCounts The read counts for that genotype
     * @return The called genotype
     */   
    public byte callSingle(double[] prob, int[] readCounts)
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
        if ((readCounts[0] + readCounts[1]) < minDepth)
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
     * @param readCounts Table of read counts
     * @return Table of called genotypes
     */    
    public byte[][] call(double[][][] probs, int[][][] readCounts)
    {
        return IntStream.range(0, probs.length).parallel().mapToObj(i -> 
        {
            int il = probs[i].length;
            byte[] r = new byte[il];
            for (int j = 0; j < il; j++)
            {
                r[j] = callSingle(probs[i][j], readCounts[i][j]);
            }
            return r;
        }).toArray(byte[][]::new);
    }
    
    private final double minDepth;
}
