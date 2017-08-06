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

package Utils.Optimize;

import java.util.Arrays;

/**
 * Optimizes the input to a function of type MultipleIntegerValue using the descent method
 * @author Daniel Money
 * @version 0.9
 */
public class Descent
{

    /**
     * Optimizes the input
     * @param v The function
     * @param initial The initial guess of best inputs
     * @param min The minimum allowed value for each input
     * @param max The maximum allowed value for each input
     * @return Optimized inputs
     * @throws Exception If something goes wrong!
     */
    public int[] optimize(MultipleIntegerValue v, int[] initial, int[] min, int[] max)
    {
        int[] current = Arrays.copyOf(initial, initial.length);
        int step = 8;
        
        double bestv = v.value(current);
        while (step > 0)
        {
            int[] besti = null;

            for (int i = 0; i < current.length; i++)
            {
                if (current[i] - step >= min[i])
                {
                    int[] testim = Arrays.copyOf(current, initial.length);
                    testim[i] = current[i] - step;
                    double testvm = v.value(testim);
                    if (testvm > bestv)
                    {
                        bestv = testvm;
                        besti = testim;
                    }
                }
                
                if (current[i] + step <= max[i])
                {
                    int[] testip = Arrays.copyOf(current, initial.length);
                    testip[i] = current[i] + step;
                    double testvp = v.value(testip);
                    if (testvp > bestv)
                    {
                        bestv = testvp;
                        besti = testip;
                    }
                }
            }
            
            if (besti == null)
            {
                step = step / 2;
            }
            else
            {
                current = besti;
            }
        }
        return current;
    }
}
