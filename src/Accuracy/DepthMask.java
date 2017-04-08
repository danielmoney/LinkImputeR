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

import Utils.Distribution.ComparableDistribution;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Creates a mask where reads for some genotypes are masked to a given depth
 * @author Daniel Money
 * @version 0.9
 */
public class DepthMask
{

    /**
     * Masks a given number of genotypes to a given depth
     * @param depths The original read counts
     * @param number The number of genotypes to mask
     * @param minDepth Only mask genotypes with more than this reqad depth
     * @param maskTo Mask to read depth
     */
    public DepthMask(int[][][] depths, int number, int minDepth, int maskTo)
    {
        this(depths,number,minDepth,ComparableDistribution.constantDistribution(maskTo), Method.ALL);
    }
    
    public DepthMask(int[][][] depths, int number, int minDepth, int maskTo, Method method)
    {
        this(depths,number,minDepth,ComparableDistribution.constantDistribution(maskTo),method);
    }
    
    public DepthMask(int[][][] depths, int number, int minDepth, ComparableDistribution<Integer> maskToDistribution)
    {
        this(depths,number,minDepth,maskToDistribution,Method.ALL);
    }
    
    /**
     * Masks a given number of genotypes to a given distribution of depths
     * @param depths The original read counts
     * @param number The number of genotypes to mask
     * @param minDepth Only mask genotypes with more than this number of reads
     * @param maskToDistribution Mask to this distribution of read depths
     */
    public DepthMask(int[][][] depths, int number, int minDepth, ComparableDistribution<Integer> maskToDistribution, Method method)
    {
        ComparableDistribution<Integer> maskTo = maskToDistribution.limitTo(0, minDepth);
        r = new Random();
        list = new ArrayList<>(number);
        
        ArrayList<SingleGenotypePosition>  selectedList = new ArrayList<>(number);
        
        switch (method)
        {
            case ALL:        
                ArrayList<SingleGenotypePosition> fullList = new ArrayList<>();
                for (int i = 0; i < depths.length; i++)
                {
                    for (int j = 0; j < depths[0].length; j++)
                    {
                        if (reads(depths[i][j]) > minDepth)
                        {
                            fullList.add(new SingleGenotypePosition(i,j));
                        }
                    }
                }

                int flSize = fullList.size();
                for (int n = 0; n < number; n++)
                {
                    selectedList.add(fullList.get(r.nextInt(flSize)));
                }
                break;
            case BYSNP:
                while (selectedList.size() < number)
                {
                    int snp = r.nextInt(depths[0].length);
                    ArrayList<SingleGenotypePosition> snpList = new ArrayList<>();
                    for (int i = 0; i < depths.length; i++)
                    {
                        if (reads(depths[i][snp]) > minDepth)
                        {
                            snpList.add(new SingleGenotypePosition(i,snp));
                        }
                    }
                    
                    if (snpList.size() > 0)
                    {
                        selectedList.add(snpList.get(r.nextInt(snpList.size())));
                    }
                }
                break;
            case BYSAMPLE:
                while (selectedList.size() < number)
                {
                    int sample = r.nextInt(depths.length);
                    ArrayList<SingleGenotypePosition> sampleList = new ArrayList<>();
                    for (int i = 0; i < depths[0].length; i++)
                    {
                        if (reads(depths[sample][i]) > minDepth)
                        {
                            sampleList.add(new SingleGenotypePosition(sample,i));
                        }
                    }
                    
                    if (sampleList.size() > 0)
                    {
                        selectedList.add(sampleList.get(r.nextInt(sampleList.size())));
                    }
                }
                break;
        }
        
        
        for (SingleGenotypePosition random: selectedList)
        {
            int i = random.getSample();
            int j = random.getSNP();
            
            list.add(new SingleGenotypeMasked(i,j,depths[i][j],mask(depths[i][j],maskTo.sample())));
        }
        
        this.depths = depths;
    }
        
    private int[] mask(int[] orig, int maskTo)
    {
        int reads = reads(orig);
        int[] masked = Arrays.copyOf(orig, orig.length);
        while (reads > maskTo)
        {
            int m = r.nextInt(reads);
            int t = 0;
            int p = 0;
            while (t + masked[p] <= m)
            {
                t += masked[p];
                p++;
            }
            masked[p] --;
            reads--;
        }
        return masked;
    }
    
    /**
     * Get an array of masked reads.  All reads are included, masked where
     * appropiate and unmasked for the other
     * @return Array of masked read
     */
    public int[][][] maskedArray()
    {
        int[][][] masked = new int[depths.length][][];
        for (int a = 0; a < depths.length; a++)
        {
            masked[a] = Arrays.copyOf(depths[a], depths[a].length);
        }
        for (SingleGenotypeMasked mg: list)
        {
            masked[mg.getSample()][mg.getSNP()] = mg.getMasked();
        }
        return masked;
    }
    
    /**
     * Get a list of masked reads
     * @return List of masked reads
     */
    public List<SingleGenotypeMasked> maskedList()
    {
        return list;
    }
    
    /**
     * Returns the number of genotypes masked
     * @return The number of masked genotypes
     */
    public int size()
    {
        return list.size();
    }
    
    private int reads(int[] depths)
    {
        return Arrays.stream(depths).sum();
    }
    
    private int[][][] depths;
    private Random r;
    private List<SingleGenotypeMasked> list;
    
    public enum Method
    {
        ALL,
        BYSNP,
        BYSAMPLE
    }
}
