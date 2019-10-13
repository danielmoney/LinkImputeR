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

package Utils;

import Callers.BinomialCaller;
import Callers.Caller;
import VCF.Exceptions.VCFDataException;
import VCF.Genotype;
import VCF.Mappers.DepthMapper;
import VCF.Position;

/**
 * Calculates minor allele frequency
 * @author Daniel Money
 * @version 1.1.3
 */
public class MAFCalculator
{
    /**
     * Constructor
     * @param minDepth A genotype must have at least this number of reads to be
     * used in the MAF calculation
     * @param maxDepth A genotype must have less than (or equal) this number of
     * reads to be used in the MAF calculation
     * @param error The error rate to be used when calling genotypes
     */
    public MAFCalculator(Caller caller, int minDepth, int maxDepth)
    {
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.caller = caller;
        dm = new DepthMapper();
    }

    public MAFCalculator(Caller caller, int minDepth)
    {
        this(caller,minDepth,Integer.MAX_VALUE);
    }

    /**
     * Calculates the minor allele frequency for a position
     * @param p The position
     * @return The minor allele frequency
     * @throws VCFDataException Thrown if the "AD" data field is not present
     */
    public double maf(Position p) throws VCFDataException
    {
        double t = 0.0;
        double c = 0.0;

        for (Genotype g: p.genotypeList())
        {
            int[] r = dm.map(g.getData("AD"));
            int trc = r[0] + r[1];
            if ((trc >= minDepth) && (trc <= maxDepth))
            {
                t += getDosage(r);
                c++;
            }
        }

        double d;
        if (c > 0)
        {
            d = t/c;
        }
        else
        {
            d = 0.0;
        }

        //Convert average dose to allele freq
        double m = d / 2.0;

        return m;
    }

    public double maf(int[][] reads)
    {
        double t = 0.0;
        double c = 0.0;

        for (int[] r: reads)
        {
            int trc = r[0] + r[1];
            if ((trc >= minDepth) && (trc <= maxDepth))
            {
                t += getDosage(r);
                c++;
            }
        }

        double d;
        if (c > 0)
        {
            d = t/c;
        }
        else
        {
            d = 0.0;
        }

        //Convert average dose to allele freq
        double m = d / 2.0;

        return m;
    }

    private double getDosage(int[] r)
    {
        double[] probs = caller.callSingle(r);
        return 2.0 * probs[0] + probs[1];
    }

    /**
     * Gets the minimum depth used in the MAF calculation
     * @return The minimum depth
     */
    public int getMinDepth()
    {
        return minDepth;
    }

    /**
     * Gets the maximum depth used in the MAF calculation
     * @return The maximum depth
     */
    public int getMaxDepth()
    {
        return maxDepth;
    }

    public Caller getCaller()
    {
        return caller;
    }

    /**
     * Gets the error used in the MAF calculation
     * @return The error
     */
//    public double getError()
//    {
//        return caller.getError();
//    }

    private DepthMapper dm;
    private Caller caller;
    private int minDepth;
    private int maxDepth;
}
