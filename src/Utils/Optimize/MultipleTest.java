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

/**
 * Optimizes the input to a function of type SingleDoubleValue
 * by testing multiple possible input values
 * @author Daniel Money
 * @version 0.9
 */
public class MultipleTest
{
    
    /**
     * Constructor
     * @param interval The interval between tested values
     */
    public MultipleTest(double interval)
    {
        this.interval = interval;
    }

    /**
     * Optimizes the input
     * @param v The function
     * @param min The minimum allowed input value
     * @param max The maximum allowed input value
     * @return Optimized input
     * @throws Exception If something goes wrong!
     */
    public double optimize(SingleDoubleValue v, double min, double max)
    {
        double bestd = Double.NaN;
        double bestval = -Double.MAX_VALUE;
        for (double d = min; d < max; d += interval)
        {
            double val = v.value(d);
            if (val > bestval)
            {
                bestval = val;
                bestd = d;
            }
        }
        
        double val = v.value(max);
        if (val > bestval)
        {
            bestd = max;
        }
        
        return bestd;
    }
    
    private double interval;
}
