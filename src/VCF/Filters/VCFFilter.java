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

import VCF.VCF;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Represents a VCF filter (eith a position filter or a sample filter)
 * @author Daniel Money
 * @version 0.9
 */
public interface VCFFilter
{
    /**
     * Apply this filter to a VCF
     * @param vcf The VCF
     */
    public void change(VCF vcf);
    
    /**
     * Gets a string summary of this filter
     * @return The string
     */
    public String getSummary();
    
    /**
     * Get the config for this filter
     * @return The config
     */
    public ImmutableNode getConfig();
}
