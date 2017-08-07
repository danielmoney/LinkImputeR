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

package VCF.Mappers;

import VCF.Exceptions.VCFUnexpectedDataException;


/**
 * Maps from a string representing read depths to an integer array of read depths
 * @author Daniel Money
 * @version 0.9
 */
public class DepthMapper implements Mapper<int[]>
{

    /**
     * Maps from string representing read depths to an integer array of read depths
     * @param s The string
     * @return The read depths
     * @throws VCF.Exceptions.VCFUnexpectedDataException If there is unexpected
     * data in the VCF
     */
    public int[] map(String s) throws VCFUnexpectedDataException
    {
        if (s.equals("."))
        {
            return new int[2];
        }
        
        String[] parts = s.split(",");
        
        if (parts.length != 2)
        {
            throw new VCFUnexpectedDataException(s + " is not a valid value for depth"
                    + " (LinkImputer currently only works on biallelic SNPs");
        }
        
        int[] result = new int[parts.length];

        for (int i = 0; i < parts.length; i++)
        {
            if (parts[i].equals("."))
            {
                result[i] = 0;
            }
            else
            {
                try
                {
                    result[i] = Integer.parseInt(parts[i]);
                }
                catch (NumberFormatException ex)
                {
                    throw new VCFUnexpectedDataException(s + " is not a valid value for depths", ex);
                }
            }
        }
        
        return result;
    }
    
    public int[][] getArray(int dim)
    {
        return new int[dim][];
    }
    
    public int[][][] get2DArray(int dim)
    {
        return new int[dim][][];
    }
}
