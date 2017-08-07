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

import Utils.SingleGenotype.SingleGenotypeCall;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypeProbability;
import Utils.SingleGenotype.SingleGenotypeReads;
import java.util.List;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Optimizes a combiner
 * @author Daniel Money
 * @version 0.9
 * @param <C> The class of the optimized combiner
 */
public interface OptimizeCombiner<C extends Combiner>
{

    /**
     * Returns an optimized combiner for the given inputs
     * @param called The called genotype probabilities
     * @param imputed The imputed genotype probabilities
     * @param reads The read depths
     * @param correct The correct genotypes
     * @param masked A list of masked genotypes
     * @return The optimized combiner
     */
    public C getOptimized(List<SingleGenotypeProbability> called,
            List<SingleGenotypeProbability> imputed,
            List<SingleGenotypeReads> reads,
            List<SingleGenotypeCall> correct,
            List<SingleGenotypeMasked> masked);
    
    /**
     * Get the config for the optimizable combiner
     * @return The config
     */
    public ImmutableNode getConfig();
}
