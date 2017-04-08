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

package Executable;

import VCF.Filters.BiallelicFilter;
import VCF.Filters.PositionFilter;
import VCF.Filters.VCFFilter;
import VCF.VCF;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Represents the input to LinkImputeR
 * @author Daniel Money
 * @version 0.9
 */
public class Input
{

    /**
     * Constructor
     * @param in The input VCF file
     * @param filters The filters to apply to the VCF as it is read in
     * @param out The file to output the VCF to IMMEDIATELY after the VCF has
     * been read in and the input parameters applied.  Input filters are applied
     * in all cases so it can save time in the final imputation step to save a VCF
     * with the filters applied rather than read it in from scratch and reapply
     * the filters.
     */
    public Input(File in, List<PositionFilter> filters, File out)
    {
        this.in = in;
        this.filters = new ArrayList<>();
        this.filters.add(new BiallelicFilter());
        this.filters.addAll(filters);
        this.out = out;
    }
    
    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */
    public Input(HierarchicalConfiguration<ImmutableNode> params)
    {
        in = new File(params.getString("filename"));
        filters = new ArrayList<>();
        filters.add(new BiallelicFilter());
        
        for (HierarchicalConfiguration<ImmutableNode> filter: params.configurationsAt("filter"))
        {
            filters.add(Available.getPositionFilter(filter));
        }
        
        String prettyString = params.getString("save",null);
        out = (prettyString == null) ? null : new File(prettyString);
    }
    
    /**
     * Get the VCF data
     * @return The VCF data
     * @throws IOException If there is a problem writing out the immediate
     * output file (see constructor)
     */
    public VCF getVCF() throws IOException
    {
        VCF vcf = new VCF(in,filters);
        if (out != null)
        {
            vcf.writeFile(out);
        }
        return vcf;
    }
    
    /**
     * Get the input config
     * @return The config
     */
    public ImmutableNode getConfig()
    {        
        ImmutableNode Iin = new ImmutableNode.Builder().name("filename").value(in).create();        
        
        ImmutableNode.Builder config = new ImmutableNode.Builder().name("input")                
                .addChild(Iin);

        for (VCFFilter filter: filters)
        {
            config.addChild(filter.getConfig());
        }
        
        if (out != null)
        {
            ImmutableNode Iout = new ImmutableNode.Builder().name("save").value(out).create();
            config.addChild(Iout);
        }        
        
        return config.create();
    }
    
    /**
     * Get the input config for the final imputation stepl 
     * @return The config
     */
    public ImmutableNode getImputeConfig()
    {
        if (out != null)
        {
            ImmutableNode Iin = new ImmutableNode.Builder().name("filename").value(out).create();
            return new ImmutableNode.Builder().name("input").addChild(Iin).create();
        }
        else
        {
            ImmutableNode Iin = new ImmutableNode.Builder().name("filename").value(in).create();        

            ImmutableNode.Builder config = new ImmutableNode.Builder().name("input")                
                    .addChild(Iin);

            for (VCFFilter filter: filters)
            {
                config.addChild(filter.getConfig());
            }
            
            return config.create();
        }
    }
    
    
    private File in;
    private List<PositionFilter> filters;
    private File out;
}
