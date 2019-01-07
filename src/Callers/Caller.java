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

package Callers;

import Utils.Progress.Progress;
import Utils.Progress.ProgressFactory;
import Utils.SingleGenotype.SingleGenotypeProbability;
import Utils.SingleGenotype.SingleGenotypeReads;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Represents a genotype caller
 * @author Daniel Money
 * @version 1.1.3
 */
public abstract class Caller
{

    /**
     * Call a single genotype
     * @param reads Array of size two with the reads for the two alleles
     * @param sample The index of the sample of the genotype in the genotype table
     * @param snp The index of tht snp of the genotypes in the genotype table
     * @return The probability of each genotype (size 3 - genotype 0, 1, 2)
     */
    public abstract double[] callSingle(int[] reads, int sample, int snp);
    
    /**
     * Calls genotypes for every genotype
     * @param reads Array of reads, dimensions are number of positions, number
     * of snps, 2 (i.e. counts for each allele)
     * @return The probability of each genotypes
     */
    public double[][][] call(int[][][] reads)
    {
        double[][][] probs = new double[reads.length][][];
        
        Progress progress = ProgressFactory.get(reads.length);
        
        IntStream.range(0, reads.length).parallel().forEach(i ->
            {
                int[][] d = reads[i];
                double[][] p = new double[d.length][];
                probs[i] = p;
                IntStream.range(0,d.length).forEach(j -> p[j] = callSingle(reads[i][j], i , j));
                progress.done();
            }
        );
        
        return probs;
    }
    
    /**
     * Call the selected genotypes
     * @param list The genotypes to call
     * @return The probabilities of each genotype
     */
    public List<SingleGenotypeProbability> call(List<SingleGenotypeReads> list)
    {
        Progress progress = ProgressFactory.get(list.size());
        return list.stream().parallel().map(sgr ->
        {
            SingleGenotypeProbability sgp = new SingleGenotypeProbability(
                sgr.getSample(), sgr.getSNP(), callSingle(sgr.getReads(), sgr.getSample(), sgr.getSNP()));
            progress.done();
            return sgp;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get the config for the caller
     * @return The config
     */
    public abstract ImmutableNode getConfig();
}
