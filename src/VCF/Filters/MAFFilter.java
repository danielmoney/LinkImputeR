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

import VCF.Mappers.DepthMapper;
import VCF.Position;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.ImmutableNode.Builder;

/**
 * Filters positions based on minor allele frequency
 * @author Daniel Money
 * @version 0.9
 */
public class MAFFilter extends PositionFilter
{

    /**
     * Constructor
     * @param maf Minor allele frequency
     * @param minDepth A genotype must have at least this number of reads to be
     * used in the MAF calculation
     * @param maxDepth A genotype must have less than (or equal) this number of 
     * reads to be used in the MAF calculation
     */
    public MAFFilter(double maf, int minDepth, int maxDepth)
    {
        this.maf = maf;
        /* A read depth of zero breaks the filter so set it to one in this case */
        this.minDepth = Math.max(1,minDepth);
        this.maxDepth = maxDepth;
    }

    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public MAFFilter(HierarchicalConfiguration<ImmutableNode> params)
    {
        this.maf = params.getDouble("maf");
        /* A read depth of zero breaks the filter so set it to one in this case */
        this.minDepth = Math.max(1,params.getInt("mindepth"));
        this.maxDepth = params.getInt("maxdepth");
    }

    public boolean test(Position p)
    {
        DepthMapper dm = new DepthMapper();
        double m = p.genotypeStream().map(g -> dm.map(g.getData("AD"))).filter(r ->
        {
            int trc = r[0] + r[1];
            return ((trc >= minDepth) && (trc <= maxDepth));
        }).mapToDouble(r -> ((double) r[1]) / ((double) (r[0] + r[1]))).average().orElse(0.0);

        return (Math.min(m,1.0-m) > maf);
    }
    
    public ImmutableNode getConfig()
    {        
        ImmutableNode Imaf = new Builder().name("maf").value(maf).create();
        ImmutableNode Imindepth = new Builder().name("mindepth").value(minDepth).create();
        ImmutableNode Imaxdepth = new Builder().name("maxdepth").value(maxDepth).create();
        
        ImmutableNode config = new Builder().name("filter")
                .addChild(Imaf)
                .addChild(Imindepth)
                .addChild(Imaxdepth)
                .addAttribute("name", "MAF")
                .create();
        
        return config;
    }
    
    public String getSummary()
    {
        return "MAF(" + maf + ")";
    }

    private int minDepth;
    private int maxDepth;
    private double maf;
}
