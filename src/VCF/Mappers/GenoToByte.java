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

import java.util.HashMap;

/**
 * Maps from a text string representing a genotype to a byte representing a genotype
 * @author Daniel Money
 */
public class GenoToByte implements ByteMapper
{

    /**
     * Default constructor
     */
    public GenoToByte()
    {
        map = new HashMap<>();
        map.put("0/0", (byte) 0);
        map.put("0/1", (byte) 1);
        map.put("1/0", (byte) 1);
        map.put("1/1", (byte) 2);
        map.put("./.", (byte) -1);
    }

    /**
     * Maps from string representing a genotype to a byte representation
     * @param s The string
     * @return Byte representation
     */
    public byte map(String s)
    {
        return map.get(s);
    }

    private HashMap<String,Byte> map;
  
}
