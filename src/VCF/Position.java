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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a position in a VCF file
 * @author Daniel Money
 * @version 0.9
 */
public class Position
{
    Position(PositionMeta meta, String[] samples, RawGenotype[] genos)
    {
        this.samples = samples;
        this.sVis = new boolean[samples.length];
        Arrays.fill(sVis, true);
        this.meta = meta;
        this.genos = genos;
    }
    
    Position(PositionMeta meta, String[] samples, boolean[] sVis, RawGenotype[] genos)
    {
        this.samples = samples;
        this.sVis = sVis;
        this.meta = meta;
        this.genos = genos;
    }
    
    /**
     * Constructor
     * @param meta Meta data for this position
     * @param genotypes Map from sample name to genotype
     */
    public Position(PositionMeta meta, LinkedHashMap<String,String> genotypes)
    {
        this.meta = meta;
        
        samples = new String[genotypes.size()];
        genos = new RawGenotype[genotypes.size()];
        
        int i = 0;
        for (Entry<String,String> g: genotypes.entrySet())
        {
            samples[i] = g.getKey();
            genos[i] = new RawGenotype(g.getValue());
            i++;
        }
        
        this.sVis = new boolean[samples.length];
        Arrays.fill(sVis, true);
    }
    
    /**
     * Get the meta data for this position
     * @return The meta data
     */
    public PositionMeta meta()
    {
        return meta;
    }
    
    /**
     * Get a stream of genotypes for this position
     * @return Stream of genotypes
     */
    public Stream<Genotype> genotypeStream()
    {
        return IntStream.range(0, samples.length).filter(i -> sVis[i])
                .mapToObj(i -> new Genotype(genos[i],meta,samples[i]));
    }

    /**
     * Get a list of genotypes for this position
     * @return List of genotypes
     */    
    public List<Genotype> genotypeList()
    {
        ArrayList<Genotype> list = new ArrayList<>();
        for (int i = 0; i < samples.length; i++)
        {
            if (sVis[i])
            {
                list.add(new Genotype(genos[i],meta,samples[i]));
            }
        }
        return list;
    }
    
    /**
     * Get a stream of sample names for this position
     * @return Stream of sample names
     */
    public String[] samples()
    {
        return IntStream.range(0, samples.length).filter(i -> sVis[i]).mapToObj(i -> samples[i]).toArray(String[]::new);
    }
    
    /**
     * Returns the position meta data as a string
     * @return String representing the position meta data
     */
    public String toString()
    {
        return meta.toString();
    }
    
    /**
     * Returns the entire position (including data) as a string
     * @return String representing the position
     */
    public String toText()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(meta.toText());
        //for (RawGenotype g: genos)
        IntStream.range(0, genos.length).filter(i -> sVis[i]).forEach(i ->
        {
            sb.append("\t");
            //sb.append(g.getInfo());
            sb.append(genos[i].getInfo());
        });
        return sb.toString();
    }
    
    RawGenotype[] getRawGenotypes()
    {
        return genos;
    }
    
    final String[] samples;
    final boolean[] sVis;
    final PositionMeta meta;
    final RawGenotype[] genos;
}
