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
 * Filters a sample based on a maximum allowed amount of missing genotypes
 * @author Daniel Money
 * @version 0.9
 */
public class SampleMissing extends SampleFilter
{

    /**
     * Constructor
     * @param threshold The missing threshold
     * @param minDepth The miminum number of reads required for a genotype to
     * be considered present
     */
    public SampleMissing(double threshold, int minDepth)
    {
        this.threshold = threshold;
        this.minDepth = minDepth;
    }
    
    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public SampleMissing(HierarchicalConfiguration<ImmutableNode> params)
    {
        this.threshold = params.getDouble("threshold");
        this.minDepth = params.getInt("mindepth");
    }
    
    public boolean test(Sample s) throws VCFDataException
    {
        DepthMapper dm = new DepthMapper();

        int c = 0;
             
        for (Genotype g: s.genotypeList())
        {
            int[] d = dm.map(g.getData("AD"));
            if ((d[0] + d[1]) < minDepth)
            {
                c++;
            }
        }

        double per = (double) c / (double) s.positions().length;
        return per < threshold;
    }
    
    public ImmutableNode getConfig()
    {        
        ImmutableNode Ithreshold = new ImmutableNode.Builder().name("threshold").value(threshold).create();
        ImmutableNode Imindepth = new ImmutableNode.Builder().name("mindepth").value(minDepth).create();
        
        ImmutableNode config = new ImmutableNode.Builder().name("filter")
                .addChild(Ithreshold)
                .addChild(Imindepth)
                .addAttribute("name", "SampleMissing")
                .create();
        
        return config;
    }
    
    public String getSummary()
    {
        return "SampleMiss(" + threshold + ")";
    }
    
    private int minDepth;
    private double threshold;
}
