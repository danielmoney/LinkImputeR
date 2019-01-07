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

package Imputers;

import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypeProbability;
import java.util.List;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Represents an imputer
 * @author Daniel Money
 * @version 0.9
 */
public interface Imputer
{

    /**
     * Imputes a genotype table
     * @param probs Genotype probabilities (from a caller)
     * @param readCounts Read counts for each genotype
     * @return Table of imputed genotype probabilities
     */
    public double[][][] impute(double[][][] probs, int[][][] readCounts);

    /**
     * Imputes a list of genotypes.  Used to calculate accuracy.  Need both
     * unmasked probs (probs) and masked probs (maskedprobs) so that the
     * calculation for each genotype is independent (i.e. using the full
     * reads for the other masked genotypes if they're used in the imputation
     * calculation).
     * @param probs Genotype probabilities (from a caller)
     * @param readCounts Read counts for each genotype
     * @param maskedprobs Genotype probabilities (from a caller) using masked
     * reads
     * @param list List of genotypes to impute
     * @return List of imputed genotype probabilities
     */
    public List<SingleGenotypeProbability> impute(double[][][] probs, int[][][] readCounts, List<SingleGenotypeProbability> maskedprobs, List<SingleGenotypeMasked> list);

    /**
     * Get the config for the imputer
     * @return The config
     */
    public ImmutableNode getConfig();
}
