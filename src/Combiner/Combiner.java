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

import Utils.SingleGenotype.SingleGenotypeProbability;
import Utils.SingleGenotype.SingleGenotypeReads;
import java.util.List;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Combines imputed and inferred genotypes to give a final called genotype
 * @author Daniel Money
 * @version 0.9
 */
public interface Combiner
{

    /**
     * Combines imputed and inferred genotypes for all genotypes in a table
     * @param called The called genotype probabilities
     * @param imputed The imputed genotype probabilities
     * @param reads The read counts for each genotype
     * @return The called probabilities
     */
    public double[][][] combine(double[][][] called, double[][][] imputed, int[][][] reads);

    /**
     * Combines imputed and inferred genotypes for a list of genotypes
     * @param called The called genotype probabilities
     * @param imputed The imputed genotype probabilities
     * @param reads The read counts for each genotype
     * @return The called probabilities
     */
    public List<SingleGenotypeProbability> combine(List<SingleGenotypeProbability> called,
            List<SingleGenotypeProbability> imputed, List<SingleGenotypeReads> reads);
    
    /**
     * Get the config for the caller
     * @return The config
     */
    public ImmutableNode getConfig();
}
