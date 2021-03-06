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

package Utils.SingleGenotype;

/**
 * Represents the genotype probabilities for a single genotype
 * @author Daniel Money
 * @version 1.1.3
 */
public class SingleGenotypeProbability extends SingleGenotypePosition
{

    /**
     * Constructor
     * @param sample The sample position
     * @param snp The snp position
     * @param prob The genotype probabilities
     */
    public SingleGenotypeProbability(int sample, int snp, double[] prob)
    {
        super(sample,snp);
        this.prob = prob;
    }
    
    /**
     * Get the genotype probabilities
     * @return The genotype probabilities
     */
    public double[] getProb()
    {
        return prob;
    }
    
    private final double[] prob;
}
