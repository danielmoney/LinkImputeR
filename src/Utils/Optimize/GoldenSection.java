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
 * Optimizes the input to a function of type SingleDoubleValue using the Golden Section method
 * @author Daniel Money
 * @version 0.9
 */
public class GoldenSection
{

    /**
     * Constructor
     * @param tol Optimize the inputs to this tolerance
     * @param ftol Optimize the value to this tolerance
     */
    public GoldenSection(double tol, double ftol)
    {
        this.ftol = ftol;
        this.tol = tol;
    }
    
    /**
     * Optimizes the input
     * @param v The function
     * @param min The minimum allowed input value
     * @param max The maximum allowed input value
     * @return Optimized input
     * @throws Exception If something goes wrong!
     */
    public double optimize(SingleDoubleValue v, double min, double max) throws Exception
    {
        double a = min;
        double b = max;
        
        double c = b - R * (b-a);
        double d = a + R * (b-a);
        
        double fc = -v.value(c);
        double fd = -v.value(d);
        
        while ((Math.abs(fc-fd) > ftol) || (d-c > tol))
        {
            if (fc < fd)
            {
                b = d;
                d = c;
                fd = fc;
                c = b - R * (b-a);
                fc = -v.value(c);
            }
            else
            {
                a = c;
                c = d;
                fc = fd;
                d = a + R * (b-a);
                fd = -v.value(d);
            }
        }
        
        if (a == min)
        {
            double fa = - v.value(a);
            if (fa < fc)
            {
                return a;
            }
        }
        
        if (b == max)
        {
            double fb = - v.value(b);
            if (fb < fd)
            {
                return b;
            }
        }
        
        return (a + b) / 2.0;
    }
    
    private double tol;
    private double ftol;
    
    private static final double R = (Math.sqrt(5) - 1.0) / 2.0;
}
