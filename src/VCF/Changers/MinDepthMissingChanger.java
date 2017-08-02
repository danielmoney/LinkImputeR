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

package VCF.Changers;

import VCF.Exceptions.VCFNoDataException;
import VCF.Exceptions.VCFUnexpectedDataException;
import VCF.Genotype;

/**
 * Changes the genotype (the GT field) of genotype to unknown (./.) if the
 * total number of reads is less than a threshold
 * @author Daniel Money
 * @version 0.9
 */
public class MinDepthMissingChanger implements GenotypeChanger
{

    /**
     * Constructor
     * @param depth The depth below which genotypes are set to unknown
     */
    public MinDepthMissingChanger(int depth)
    {
        this.depth = depth;
    }
    
    public void change(Genotype g) throws VCFNoDataException, VCFUnexpectedDataException
    {
        String data = g.getData("DP");
        try
        {
            if (data.equals(".") || (Integer.parseInt(data) < depth))
            {
                g.replaceData("GT", "./.");
            }
        }
        catch (NumberFormatException ex)
        {
            throw new VCFUnexpectedDataException(data + " is not a valid value for DP", ex);
        }
    }
    
    private int depth;
}
