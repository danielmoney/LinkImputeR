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
 * Represents an imputer that can be optimized
 * @author Daniel Money
 * @param <I> The class of the optimized imputer
 */
public interface OptimizeImputer<I extends Imputer>
{

    /**
     * Get an optimized version of the imputer
     * @param callprobs Original called genotype probabilities
     * @param readCounts Read counts
     * @param maskedprob Masked genotype probabilities
     * @param list List of masked sites and their genotype
     * @return An optimized imputer
     */
    public I getOptimized(double[][][] callprobs, int[][][] readCounts, List<SingleGenotypeProbability> maskedprob, List<SingleGenotypeMasked> list);

 
    /**
     * Get the config for this case
     * @return The config
     */
    public ImmutableNode getConfig();
}
