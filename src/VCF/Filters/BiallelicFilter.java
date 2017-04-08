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

import VCF.Position;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Filters positions for biallelic positions
 * @author Daniel Money
 * @version 0.9
 */
public class BiallelicFilter extends PositionFilter
{

    /**
     * Constructor
     */
    public BiallelicFilter()
    {
        
    }
    
    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public BiallelicFilter(HierarchicalConfiguration<ImmutableNode> params)
    {
        
    }
    
    public boolean test(Position p)
    {
        return ((p.meta().getAlt().length  == 1) && !p.meta().getAlt()[0].equals("."));
    }
    
    public ImmutableNode getConfig()
    {
        ImmutableNode config = new ImmutableNode.Builder().name("filter")
                .addAttribute("name", "BiallelicGap")
                .create();
        
        return config;
    }
    
    public String getSummary()
    {
        return "BiallelicGap()";
    }
}
