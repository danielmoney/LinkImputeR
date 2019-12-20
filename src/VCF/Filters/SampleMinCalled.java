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

package VCF.Filters;

import VCF.Exceptions.VCFDataException;
import VCF.Genotype;
import VCF.Mappers.DepthMapper;
import VCF.Sample;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Filters a sample based on a minimum number of called genotypes
 * @author Daniel Money
 * @version 1.1.3
 */
public class SampleMinCalled extends SampleFilter
{

    /**
     * Constructor
     * @param minCalled The minimum number of called genotypes
     * @param minDepth The minimum number of reads required for a genotype to
     * be considered present
     */
    public SampleMinCalled(int minCalled, int minDepth)
    {
        this.minCalled = minCalled;
        this.minDepth = minDepth;
    }

    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */
    public SampleMinCalled(HierarchicalConfiguration<ImmutableNode> params)
    {
        this.minCalled = params.getInt("mincalled");
        this.minDepth = params.getInt("mindepth");
    }
    
    public boolean test(Sample s) throws VCFDataException
    {
        DepthMapper dm = new DepthMapper();

        int c = 0;
             
        for (Genotype g: s.genotypeList())
        {
            int[] d = dm.map(g.getData("AD"));
            if ((d[0] + d[1]) >= minDepth)
            {
                c++;
                if (c == minCalled)
                {
                    return true;
                }
            }
        }

        return false;
    }
    
    public ImmutableNode getConfig()
    {        
        ImmutableNode Ithreshold = new ImmutableNode.Builder().name("mincalled").value(minCalled).create();
        ImmutableNode Imindepth = new ImmutableNode.Builder().name("mindepth").value(minDepth).create();
        
        ImmutableNode config = new ImmutableNode.Builder().name("filter")
                .addChild(Ithreshold)
                .addChild(Imindepth)
                .addAttribute("name", "SampleMinCalled")
                .create();
        
        return config;
    }
    
    public String getSummary()
    {
        return "SampleMinCall(" + minCalled + ")";
    }
    
    private final int minDepth;
    private final int minCalled;
}
