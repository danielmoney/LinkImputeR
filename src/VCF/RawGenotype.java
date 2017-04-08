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

import java.util.Arrays;

class RawGenotype
{
    RawGenotype(String data)
    {
        this.data = data.getBytes();
    }
    
    RawGenotype(byte[] data)
    {
        this.data = data;
    }
    
    String getInfo()
    {
        return new String(data);
    }
    
    void setInfo(String data)
    {
        this.data = data.getBytes();
    }
    
    RawGenotype copy()
    {
        return new RawGenotype(Arrays.copyOf(data, data.length));
    }
    
    private byte[] data;
}
