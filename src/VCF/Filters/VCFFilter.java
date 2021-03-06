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
import VCF.VCF;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Represents a VCF filter (either a position filter or a sample filter)
 * @author Daniel Money
 * @version 1.1.3
 */
public interface VCFFilter
{
    /**
     * Apply this filter to a VCF
     * @param vcf The VCF
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    void change(VCF vcf) throws VCFDataException;
    
    /**
     * Gets a string summary of this filter
     * @return The string
     */
    String getSummary();
    
    /**
     * Get the config for this filter
     * @return The config
     */
    ImmutableNode getConfig();
}
