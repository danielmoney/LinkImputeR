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

package VCF;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple convenience class used to convert a byte genotype to a text string
 * representing it
 * @author Daniel Money
 * @version 0.9
 */
public class ByteToGeno
{

    /**
     * Default constructor
     */
    
    //This should probably be a static class
    public ByteToGeno()
    {
        map = new HashMap<>();
        map.put((byte) 0, "0/0");
        map.put((byte) 1, "0/1");
        map.put((byte) 2, "1/1");
        map.put((byte) -1, "./.");
    }
    
    /**
     * Maps form byte to string
     * @param b The byte representing the genotype
     * @return The string representing the genotype
     */
    public String map(Byte b)
    {
        return map.get(b);
    }
    
    private Map<Byte,String> map;
}
