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

import VCF.Genotype;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Changes the genotype (the GT field) of genotype to unknown (./.) if the
 * total number of reads is less than a threshold
 * @author Daniel Money
 * @version 0.9
 */
public class DepthMissingChanger implements GenotypeChanger
{

    /**
     * Constructor
     * @param depth The depth below which genotypes are set to unknown
     */
    public DepthMissingChanger(int depth)
    {
        this.depth = depth;
    }
    
    public void change(Genotype g)
    {
        int d = NumberUtils.toInt(g.getData("DP"));
        if (d < depth)
        {
            g.replaceData("GT", "./.");
        }
    }
    
    private int depth;
}
