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

package Accuracy;

import Utils.SingleGenotype.SingleGenotypeCall;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypePosition;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Calculates accuracy statistics
 * @author Daniel Money
 * @version 0.9
 */
public class AccuracyCalculator
{
    private AccuracyCalculator()
    {
        
    }
    
    /**
     * Calculates the percentage of genotypes called correctly
     * @param correct The correct genotypes
     * @param compareTo The genotypes to test
     * @return Percentage accuracy
     */
    public static double accuracy(List<SingleGenotypeCall> correct, List<SingleGenotypeCall> compareTo)
    {
        if (!SingleGenotypePosition.samePositions(correct, compareTo))
        {
            //SHOULD DO SOMETHING PROPER HERE
            throw new RuntimeException();
        }
        
        return IntStream.range(0, correct.size()).mapToDouble(i -> 
            (correct.get(i).getCall() == compareTo.get(i).getCall()) ? 1.0 : 0.0).average().orElse(0.0);
    }
    
    public static double correlation(List<SingleGenotypeCall> correct, List<SingleGenotypeCall> compareTo)
    {
        if (!SingleGenotypePosition.samePositions(correct, compareTo))
        {
            //SHOULD DO SOMETHING PROPER HERE
            throw new RuntimeException();
        }
        
        int[][] counts = new int[3][3];
        int c = 0;
        for (int i = 0; i < correct.size(); i++)
        {
            counts[correct.get(i).getCall()][compareTo.get(i).getCall()] ++;
            c ++;
        }
        
        int tota = counts[1][0] + counts[1][1] + counts[1][2] +
                2 * (counts[2][0] + counts[2][1] + counts[2][2]);
        double meana = (double) tota / (double) c;
        
        int totb = counts[0][1] + counts[1][1] + counts[2][1] +
                2 * (counts[0][2] + counts[1][2] + counts[2][2]);
        double meanb = (double) totb / (double) c;
        
        double xy = 0.0;
        double xx = 0.0;
        double yy = 0.0;
        
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                xy += (double) counts[i][j] * ((double) i - meana) * ((double) j - meanb);
                xx += (double) counts[i][j] * ((double) i - meana) * ((double) i - meana);
                yy += (double) counts[i][j] * ((double) j - meanb) * ((double) j - meanb);
            }
        }
        
        return (xy * xy) / (xx * yy);
    }
    
    /**
     * Calculates various statistics concerning the genotypes called correctly
     * @param correct The correct egnotypes
     * @param compareTo the genotypes to test
     * @param depths The depth (i.e. number of reads) used for each genotype
     * @return Accuracy statistics
     */
    public static AccuracyStats accuracyStats(List<SingleGenotypeCall> correct, List<SingleGenotypeCall> compareTo,
            List<SingleGenotypeMasked> depths)
    {
        if (!SingleGenotypePosition.samePositions(correct, compareTo) ||
                !SingleGenotypePosition.samePositions(correct, depths))
        {
            //SHOULD DO SOMETHING PROPER HERE
            throw new RuntimeException();
        }
        
        AccuracyStats stats = new AccuracyStats();
        
        IntStream.range(0, correct.size()).forEach(i ->
            stats.add(correct.get(i).getCall(), compareTo.get(i).getCall(), depths.get(i).getMaskedDepth()));
        
        return stats;
    }
    
    public enum AccuracyMethod
    {
        Accuracy,
        Correlation
    }
}
