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

package VCF;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a sample in a VCF file
 * @author Daniel Money
 * @version 1.1.3
 */
public class Sample
{
    Sample(String sample, PositionMeta[] positions, boolean[] pVis, RawGenotype[] genos)
    {
        this.sample = sample;
        this.positions = positions;
        this.pVis = pVis;
        this.genos = genos;
    }
    
    /**
     * Gets the name of the sample
     * @return The name
     */
    public String name()
    {
        return sample;
    }
    
    /**
     * Gets an array of positions in the sample
     * @return Array of positions
     */
    public PositionMeta[] positions()
    {
        return IntStream.range(0, positions.length).filter(i -> pVis[i]).mapToObj(i -> positions[i]).toArray((IntFunction<PositionMeta[]>) PositionMeta[]::new);
    }
    
    /**
     * Get a stream of genotypes for this sample
     * @return Stream of genotypes
     */
    public Stream<Genotype> genotypeStream()
    {
        return IntStream.range(0, positions.length).filter(i -> pVis[i])
                .mapToObj(i -> new Genotype(genos[i],positions[i],sample));
    }
    
    /**
     * Get a list of genotypes for this sample
     * @return List of genotypes
     */
    public List<Genotype> genotypeList()
    {
        List<Genotype> list = new ArrayList<>();
        for (int i = 0; i < positions.length; i++)
        {
            if (pVis[i])
            {
                list.add(new Genotype(genos[i],positions[i],sample));
            }
        }
        return list;
    }
    
    final String sample;
    final PositionMeta[] positions;
    final boolean[] pVis;
    final RawGenotype[] genos;
}
