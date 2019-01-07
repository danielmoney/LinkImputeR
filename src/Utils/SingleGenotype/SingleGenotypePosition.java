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

import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents the position of a single genotype
 * @author Daniel Money
 * @version 0.9
 */
public class SingleGenotypePosition
{

    /**
     * Constructor
     * @param sample The sample position
     * @param snp The snp position
     */
    public SingleGenotypePosition(int sample, int snp)
    {
        this.sample = sample;
        this.snp = snp;
    }
    
    /**
     * Get the sample position
     * @return The sample position
     */
    public int getSample()
    {
        return sample;
    }
    
    /**
     * Get the snp position
     * @return The snp position
     */
    public int getSNP()
    {
        return snp;
    }
    
    private int sample;
    private int snp;
    
    /**
     * Tests whether two positions represent the same genotype
     * @param pos1 First position
     * @param pos2 Second position
     * @return Whether the two positions represent the same genotypes
     */
    public static boolean samePosition(SingleGenotypePosition pos1, SingleGenotypePosition pos2)
    {
        return (pos1.getSample() == pos2.getSample()) &&
                        (pos1.getSNP() == pos2.getSNP());
    }
    
    /**
     * Tests whether two lists represent the same genotypes in the same order
     * @param list1 First list of genotypes
     * @param list2 Second list of genotypes
     * @return Whether the lists represent the same genotypes
     */
    public static boolean samePositions(List<? extends SingleGenotypePosition> list1, List<? extends SingleGenotypePosition> list2)
    {
        if (list1.size() != list2.size())
        {
            return false;
        }
        if (IntStream.range(0, list1.size()).allMatch(i ->
                  samePosition(list1.get(i),list2.get(i))))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
