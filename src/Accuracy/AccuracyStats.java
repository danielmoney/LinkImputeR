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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * Represents accuracy statistics 
 * @author Daniel Money
 * @version 1.1.3
 */
public class AccuracyStats
{
    AccuracyStats()
    {
        total = new CorrectCount();
        byDepth = new CorrectCountMap();
        byGeno = new CorrectCountMap();
        byDepthGeno = new TreeMap<>();
        maxDepth = 0;
    }
    
    synchronized void add(byte original, byte imputed, int depth, double maf)
    {
        total.add(original,imputed,maf);
        byDepth.add(depth, original, imputed, maf);
        byGeno.add(original, original, imputed, maf);
        if (!byDepthGeno.containsKey(depth))
        {
            byDepthGeno.put(depth, new CorrectCountMap());
        }
        byDepthGeno.get(depth).add(original,original,imputed,maf);
        maxDepth = Math.max(depth, maxDepth);
    }
    
    /**
     * Returns the overall accuracy
     * @return The overall accuracy
     */
    public double accuracy()
    {
        return total.getAccuracy();
    }
    
    /**
     * Returns the overall correlation
     * @return The overall correlation
     */
    public double correlation()
    {
        return total.getCorrelation();
    }
    
    /**
     * Returns the accuracy for a certain depth (number of reads)
     * @param depth The depth to return the accuracy for
     * @return The accuracy
     */
    public double depthAccuracy(int depth)
    {
        if (byDepth.has(depth))
        {
            return byDepth.get(depth).getAccuracy();
        }
        else
        {
            return -1.0;
        }
    }

    /**
     * Returns the correlation for a certain depth (number of reads)
     * @param depth The depth to return the accuracy for
     * @return The correlation
     */    
    public double depthCorrelation(int depth)
    {
        if (byDepth.has(depth))
        {
            return byDepth.get(depth).getCorrelation();
        }
        else
        {
            return -1.0;
        }
    }
    
    /**
     * Return the accuracy for a given genotype
     * @param geno The true genotype to return the accuracy for
     * @return The accuracy
     */
    public double genoAccuracy(byte geno)
    {
        if (byGeno.has(geno))
        {
            return byGeno.get(geno).getAccuracy();
        }
        else
        {
            return -1;
        }
    }
    
    /**
     * Return the accuracy for a given depth (read count) and true genotype
     * @param depth The depth to return the accuracy for
     * @param geno The true genotype to return the accuracy for
     * @return The accuracy
     */
    public double depthGenoAccuracy(int depth, byte geno)
    {
        if (byDepthGeno.containsKey(depth) && byDepthGeno.get(depth).has(geno))
        {
            return byDepthGeno.get(depth).get(geno).getAccuracy();
        }
        else
        {
            return -1.0;
        }
    }
    
    /**
     * Return the total number of genotypes used in the accuracy calculation
     * @return The number of genotypes
     */
    public int total()
    {
        return total.getTotal();
    }
    
    /**
     * Return the number of genotypes used in the accuracy calculation that have
     * the given depth (read count)
     * @param depth Depth
     * @return The number of genotypes
     */
    public int depthTotal(int depth)
    {
        if (byDepth.has(depth))
        {
            return byDepth.get(depth).getTotal();
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Return the number of genotypes used in the accuracy calculation that have
     * the give true genotype
     * @param geno The true genotype
     * @return The number of genotypes
     */
    public int genoTotal(byte geno)
    {
        if (byGeno.has(geno))
        {
            return byGeno.get(geno).getTotal();
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Return the number of genotypes used in the accuracy calculation that have
     * the give depth (read count) and true genotype
     * @param depth Depth
     * @param geno The true genotype
     * @return The number of genotypes
     */
    public int depthGenoTotal(int depth, byte geno)
    {
        if (byDepthGeno.containsKey(depth) && byDepthGeno.get(depth).has(geno))
        {
            return byDepthGeno.get(depth).get(geno).getTotal();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Return the total number of genotypes that are correct
     * @return The number of genotypes
     */
    public int correct()
    {
        return total.getCorrect();
    }

    /**
     * Return the number of genotypes that are correct that have
     * the given depth (read count)
     * @param depth Depth
     * @return The number of genotypes
     */
    public int depthCorrect(int depth)
    {
        if (byDepth.has(depth))
        {
            return byDepth.get(depth).getCorrect();
        }
        else
        {
            return -1;
        }
    }

    /**
     * Return the number of genotypes that are correct that have
     * the give true genotype
     * @param geno The true genotype
     * @return The number of genotypes
     */
    public int genoCorrect(byte geno)
    {
        if (byGeno.has(geno))
        {
            return byGeno.get(geno).getCorrect();
        }
        else
        {
            return -1;
        }
    }
    
    /**
     * Return the number of genotypes that are correct that have
     * the given depth (read count) and true genotype
     * @param depth Depth
     * @param geno The true genotype
     * @return The number of genotypes
     */
    public int depthGenoCorrect(int depth, byte geno)
    {
        if (byDepthGeno.containsKey(depth) && byDepthGeno.get(depth).has(geno))
        {
            return byDepthGeno.get(depth).get(geno).getCorrect();
        }
        else
        {
            return -1;
        }
    }
    
    /**
     * Gte the maximum depth (read count) of any genotype used in the accuracy
     * calculation
     * @return The maximum depth
     */
    public int getMaxDepth()
    {
        return maxDepth;
    }
    
    private int maxDepth;
    private final CorrectCount total;
    private final CorrectCountMap byDepth;
    private final CorrectCountMap byGeno;
    private final Map<Integer,CorrectCountMap> byDepthGeno;
    
    private class CorrectCount
    {
        public CorrectCount()
        {
            counts = new int[3][3];
            scaledOriginal = new ArrayList<>();
            scaledImputed = new ArrayList<>();
        }
        
        public int getCorrect()
        {
            return counts[0][0] + counts[1][1] + counts[2][2];
        }
        
        public int getTotal()
        {
            return t;
        }
        
        public double getAccuracy()
        {
            return (double) getCorrect() / (double) t;
        }
        
        public double getCorrelation()
        {
            double meanx = scaledOriginal.stream().mapToDouble(d -> d).summaryStatistics().getAverage();
            double meany = scaledImputed.stream().mapToDouble(d -> d).summaryStatistics().getAverage();
            
            double xx = scaledOriginal.stream().mapToDouble(d -> (d - meanx) * (d - meanx)).sum();
            double yy = scaledImputed.stream().mapToDouble(d -> (d - meany) * (d - meany)).sum();
            double xy = IntStream.range(0, scaledOriginal.size())
                    .mapToDouble(i -> (scaledOriginal.get(i) - meanx) * (scaledImputed.get(i) - meany))
                    .sum();            
            
            return (xy * xy) / (xx * yy);
        }
        
        public void add(byte original, byte imputed, double maf)
        {
            t++;
            counts[original][imputed] ++;
            scaledOriginal.add((double) original - maf);
            scaledImputed.add((double) imputed - maf);
        }
        
        final List<Double> scaledOriginal;
        final List<Double> scaledImputed;
        final int[][] counts;
        int t;
    }
    
    private class CorrectCountMap
    {
        public CorrectCountMap()
        {
            map = new TreeMap<>();
        }
        
        public void add(int i, byte original, byte imputed, double maf)
        {
            if (!map.containsKey(i))
            {
                map.put(i,new CorrectCount());
            }
            map.get(i).add(original, imputed, maf);
        }
        
        public boolean has(int i)
        {
            return map.containsKey(i);
        }
        
        public CorrectCount get(int i)
        {
            return map.get(i);
        }
        
        private final Map<Integer,CorrectCount> map;
    }
}

