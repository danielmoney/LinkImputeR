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

import java.util.Arrays;

/**
 * Represents a single masked genotype
 * @author Daniel Moneu
 * @version 0.9
 */
public class SingleGenotypeMasked extends SingleGenotypePosition
{

    /**
     * Constructor
     * @param sample The sample position
     * @param snp The snp position
     * @param original The original read counts
     * @param maskedTo The masked read counts
     * @param maf The minor allele frequency for the position of this genotype
     */
    public SingleGenotypeMasked(int sample, int snp, int[] original, int[] maskedTo, double maf)
    {
        super(sample,snp);
        this.original = original;
        this.maskedTo = maskedTo;
        this.maf = maf;
    }
    
    /**
     * Get the msked read counts
     * @return The masked read counts
     */
    public int[] getMasked()
    {
        return maskedTo;
    }
    
    /**
     * Get the masked depth
     * @return The masked depth
     */
    public int getMaskedDepth()
    {
        return Arrays.stream(maskedTo).sum();
    }
    
    /**
     * Get the original read counts
     * @return The original read counts
     */
    public int[] getOriginal()
    {
        return original;
    }
    
    /**
     * Get the minor allele frequency for the position of this genotypes
     * @return The minor allele frequency
     */
    public double getMaf()
    {
        return maf;
    }
    
    private final int[] original;
    private final int[] maskedTo;
    private final double maf;
}
