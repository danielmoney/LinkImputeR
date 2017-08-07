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

import Accuracy.DepthMask.Method;
import Callers.Caller;
import Exceptions.NotEnoughMaskableGenotypesException;
import Utils.Distribution.ComparableDistribution;
import Utils.Distribution.ComparableDistributionCollector;
import Utils.SingleGenotype.SingleGenotypePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Utility class for easily creating multiple DepthMasks with the same parameters
 * @author daniel
 */
public class DepthMaskFactory
{

    /**
     * Default constructor
     * @param number The number of genotypes to makes reads from
     * @param minDepth Only mask genotypes with a greater read depth than this
     * @param limitDist The distribution to mask the reads to
     * @param method The method to be used to select genotypes to be masked
     */
    public DepthMaskFactory(int number, int minDepth, int limitDist, Method method)
    {
        this.number = number;
        this.minDepth = minDepth;
        this.limitDist = limitDist;
        this.method = method;
    }
    
    /**
     * Creates the factory from the given configuration
     * @param params The configuration
     */
    public DepthMaskFactory(HierarchicalConfiguration<ImmutableNode> params)
    {
        this.number = params.getInt("number");
        this.minDepth = params.getInt("mindepth");
        this.limitDist = params.getInt("limitdist");
        switch(params.getString("method","all").toLowerCase())
        {
            case "all":
                method = Method.ALL;
                break;
            case "bysnp":
                method = Method.BYSNP;
                break;
            case "bysample":
                method = Method.BYSAMPLE;
                break;
        }
    }
    
    /**
     * Get a depth mask
     * @param readCounts The reads to mask
     * @param dontUse A list of genotypes not to mask
     * @param caller The genotype caller to use
     * @return A depth mask
     * @throws NotEnoughMaskableGenotypesException If there is not enough maskable genotypes
     */
    public DepthMask getDepthMask(int[][][] readCounts, List<SingleGenotypePosition> dontUse, Caller caller)  throws NotEnoughMaskableGenotypesException
    {
        ComparableDistribution<Integer> fulldist = Arrays.stream(readCounts).parallel().flatMap(rc -> Arrays.stream(rc).map(r -> r[0] + r[1])).collect(new ComparableDistributionCollector<>());
        ComparableDistribution<Integer> dist = fulldist.limitTo(0, limitDist);
        
        return new DepthMask(readCounts,number,minDepth,dist,method,dontUse, caller);
    }
    
    /**
     * Get a depth mask
     * @param readCounts The reads to mask
     * @param caller The genotype caller to use
     * @return A depth mask
     * @throws NotEnoughMaskableGenotypesException If there is not enough maskable genotypes
     */
    public DepthMask getDepthMask(int[][][] readCounts, Caller caller)  throws NotEnoughMaskableGenotypesException
    {
        return getDepthMask(readCounts, new ArrayList<>(), caller);
    }
    
    /**
     * Get the config for the factory
     * @return The config
     */
    public ImmutableNode getConfig()
    {        
        ImmutableNode Inum = new ImmutableNode.Builder().name("number").value(number).create();
        ImmutableNode Imin = new ImmutableNode.Builder().name("mindepth").value(minDepth).create(); 
        ImmutableNode Ilim = new ImmutableNode.Builder().name("limitdist").value(limitDist).create(); 
        
        String m;
        switch (method)
        {
            case BYSNP:
                m = "bysnp";
                break;
            case BYSAMPLE:
                m = "bysample";
                break;
            default:
                m = "all";
                break;
        }
        
        ImmutableNode Imethod = new ImmutableNode.Builder().name("method").value(m).create();
        
        return new ImmutableNode.Builder().name("mask")                
                .addChild(Inum).addChild(Imin).addChild(Ilim).addChild(Imethod).create();
    }
    
    private Method method;
    private int number;
    private int minDepth;
    private int limitDist;
}
