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

package Executable;

import Accuracy.AccuracyStats;
import Exceptions.OutputException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.IntStream;

import Utils.SingleGenotype.SingleGenotypeCall;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypePosition;
import VCF.PositionMeta;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Class used to print stats
 * @author Daniel Money
 * @version 0.9
 */
public class PrintStats
{

    /**
     * Constructor
     * @param pretty File to print pretty stats to
     * @param depth File to print depth stats to
     * @param geno File to print geno stats to
     * @param depthGeno File to print depth-geno stats to
     * @param eachMasked File to print information for each masked sample / position to
     * @param partial Whether to print partial stats (i.e. for imputed and called
     * as well as for combined)
     */
    public PrintStats(File pretty, File depth, File geno, File depthGeno, File eachMasked, boolean partial)
    {
        this.pretty = pretty;
        this.depth = depth;
        this.geno = geno;
        this.depthGeno = depthGeno;
        this.eachMasked = eachMasked;
        this.partial = partial;
    }
    
    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public PrintStats(HierarchicalConfiguration<ImmutableNode> params)
    {
        String prettyString = params.getString("pretty",null);
        pretty = (prettyString == null) ? null : new File(prettyString);

        String depthString = params.getString("depth",null);
        depth = (depthString == null) ? null : new File(depthString);
        
        String genoString = params.getString("geno",null);
        geno = (genoString == null) ? null : new File(genoString);
        
        String depthGenoString = params.getString("depthgeno",null);
        depthGeno = (depthGenoString == null) ? null : new File(depthGenoString);

        String eachMaskedString = params.getString("eachmasked",null);
        eachMasked = (eachMaskedString == null) ? null : new File(eachMaskedString);
        
        partial = params.getBoolean("partial",false);
    }
    
    /**
     * Constructor that creates file names for the different output based on a
     * root string
     * @param root The root string
     * @param partial Whether to print partial stats (i.e. for imputed and called
     * as well as for combined)
     */
    public PrintStats(String root, boolean partial)
    {
        pretty = new File(root + "_pretty.dat");
        depth = new File(root + "_depth.dat");
        geno = new File(root + "_geno.dat");
        depthGeno = new File(root + "_dg.dat");
        eachMasked = new File(root + "_each.dat");
        this.partial = partial;
    }
    
    /**
     * Write stats to the appropriate files
     * @param stats Accuracy stats for combined
     * @param cstats Accuracy stats for called
     * @param istats Accuracy stats for imputed
     * @throws OutputException If there is an IO problem
     */
    public void writeStats(AccuracyStats stats, AccuracyStats cstats, AccuracyStats istats) throws OutputException
    {
        int maxDepth = stats.getMaxDepth();
        
        if (pretty != null)
        {
            try
            {
                PrintWriter prettyWriter = new PrintWriter(new BufferedWriter(
                        new FileWriter(pretty)));

                //TOTAL
                prettyWriter.print("Overall accuracy: " + dformat(stats.accuracy()) + "\t(" + iformat(stats.total()) + ")");
                if (partial)
                {
                    prettyWriter.print("\t\t[" + dformat(cstats.accuracy()) + "/" + dformat(istats.accuracy()) + "]");
                }
                prettyWriter.println();
                prettyWriter.println();

                //BY DEPTH
                prettyWriter.println("By depth");
                for (int i = 0; i <= maxDepth; i++)
                {
                    prettyWriter.print(i + "\t" + dformat(stats.depthAccuracy(i)) + "\t(" + iformat(stats.depthTotal(i)) + ")");
                    if (partial)
                    {
                        prettyWriter.print("\t\t[" + dformat(cstats.depthAccuracy(i)) + "/" + dformat(istats.depthAccuracy(i)) + "]");
                    }
                    prettyWriter.println();
                }
                prettyWriter.println();

                //BY GENOTYPE
                prettyWriter.println("By genotype");
                for (byte i = 0; i < 3; i++)
                {
                    prettyWriter.print(i + "\t" + dformat(stats.genoAccuracy(i)) + "\t(" + iformat(stats.genoTotal(i)) + ")");
                    if (partial)
                    {
                        prettyWriter.print("\t\t[" + dformat(cstats.genoAccuracy(i)) + "/" + dformat(istats.genoAccuracy(i)) + "]");
                    }
                    prettyWriter.println();
                }
                prettyWriter.println();

                //BY DEPTH AND GENOTYPE
                prettyWriter.println("By depth and genotype");
                prettyWriter.println("\t0\t\t1\t\t2");
                for (int i = 0; i <= maxDepth; i++)
                {
                    prettyWriter.print(i + "\t" +
                            dformat(stats.depthGenoAccuracy(i, (byte) 0)) + "\t" +
                            dformat(stats.depthGenoAccuracy(i, (byte) 1)) + "\t" +
                            dformat(stats.depthGenoAccuracy(i, (byte) 2)) + "\t(" +
                            iformat(stats.depthGenoTotal(i, (byte) 0)) + "\t" +
                            iformat(stats.depthGenoTotal(i, (byte) 1)) + "\t" +
                            iformat(stats.depthGenoTotal(i, (byte) 2)) + ")");

                    if (partial)
                    {
                        prettyWriter.print("\t\t[" +
                                dformat(cstats.depthGenoAccuracy(i, (byte) 0)) + "/" + 
                                dformat(istats.depthGenoAccuracy(i, (byte) 0)) + "\t" +
                                dformat(cstats.depthGenoAccuracy(i, (byte) 1)) + "/" + 
                                dformat(istats.depthGenoAccuracy(i, (byte) 1)) + "\t" +
                                dformat(cstats.depthGenoAccuracy(i, (byte) 2)) + "/" +
                                dformat(istats.depthGenoAccuracy(i, (byte) 2)) + "]");
                    }
                    prettyWriter.println();
                }

                prettyWriter.println();
                prettyWriter.println();
                prettyWriter.println();

                //TOTAL
                prettyWriter.print("Overall correlation: " + dformat(stats.correlation()) + "\t(" + iformat(stats.total()) + ")");
                if (partial)
                {
                    prettyWriter.print("\t\t[" + dformat(cstats.correlation()) + "/" + dformat(istats.correlation()) + "]");
                }
                prettyWriter.println();
                prettyWriter.println();

                //BY DEPTH
                prettyWriter.println("By depth");
                for (int i = 0; i <= maxDepth; i++)
                {
                    prettyWriter.print(i + "\t" + dformat(stats.depthCorrelation(i)) + "\t(" + iformat(stats.depthTotal(i)) + ")");
                    if (partial)
                    {
                        if (i == 0)
                        {
                            prettyWriter.print("\t\t[-.----/" + dformat(istats.depthCorrelation(i)) + "]");
                        }
                        else
                        {
                            prettyWriter.print("\t\t[" + dformat(cstats.depthCorrelation(i)) + "/" + dformat(istats.depthCorrelation(i)) + "]");
                        }
                    }
                    prettyWriter.println();
                }
                prettyWriter.println();

                prettyWriter.close();
            }
            catch (IOException ex)
            {
                throw new OutputException("Problem writing pretty stats", ex);
            }
        }
        
        //BY DEPTH
        if (depth != null)
        {
            try
            {
                PrintWriter depthWriter = new PrintWriter(new BufferedWriter(
                        new FileWriter(depth)));
                for (int i = 0; i <= maxDepth; i++)
                {
                    depthWriter.println(i + "\t" + stats.depthAccuracy(i) + "\t" + stats.depthCorrelation(i));
                }
                depthWriter.close();
                        }
            catch (IOException ex)
            {
                throw new OutputException("Problem writing depth stats", ex);
            }
        }
        
        //BY GENOTYPE
        if (geno != null)
        {
            try
            {
                PrintWriter genoWriter = new PrintWriter(new BufferedWriter(
                        new FileWriter(geno)));
                for (byte i = 0; i < 3; i++)
                {
                    genoWriter.println(i + "\t" + stats.genoAccuracy(i));
                }
                genoWriter.close();
            }
            catch (IOException ex)
            {
                throw new OutputException("Problem writing genotype stats", ex);
            }
        }
        
        //BY DEPTH AND GENOTYPE
        if (depthGeno != null)
        {
            try
            {
                PrintWriter depthGenoWriter = new PrintWriter(new BufferedWriter(
                        new FileWriter(depthGeno)));
                for (int i = 0; i <= maxDepth; i++)
                {
                    depthGenoWriter.println(i + "\t" +
                            stats.depthGenoAccuracy(i, (byte) 0) + "\t" +
                            stats.depthGenoAccuracy(i, (byte) 1) + "\t" +
                            stats.depthGenoAccuracy(i, (byte) 2));
                }
                depthGenoWriter.close();
            }
            catch (IOException ex)
            {
                throw new OutputException("Problem writing depth/genotype stats", ex);
            }
        }
    }

    /**
     * Writes data on each masked sample / snp if appropriate
     * @param correct List of correct genotypes
     * @param imputed List of imputed genotypes
     * @param samples List of samples (sample names)
     * @param positions List of positions (position meta data)
     * @throws OutputException If there is an IO problem
     */
    public void writeEachMasked(List<SingleGenotypeCall> correct, List<SingleGenotypeCall>  imputed,
                                String[] samples, PositionMeta[] positions) throws OutputException
    {
        if (eachMasked != null)
        {
            try
            {
                if (!SingleGenotypePosition.samePositions(correct, imputed))
                {
                    //SHOULD DO SOMETHING PROPER HERE
                    throw new RuntimeException();
                }

                PrintWriter eachMaskedWriter = new PrintWriter(new BufferedWriter(
                        new FileWriter(eachMasked)));
                eachMaskedWriter.println("SNP\tSample\tTrue\tImputed");
                for (int i = 0; i < correct.size(); i++)
                {
                    PositionMeta p = positions[correct.get(i).getSNP()];
                    eachMaskedWriter.println(samples[correct.get(i).getSample()] + "\t" +
                            p.getChrom() + ":" + p.getPosition() + "\t" +
                            correct.get(i).getCall() + "\t" +
                            imputed.get(i).getCall());
                }
                eachMaskedWriter.close();
            }
            catch (IOException ex)
            {
                throw new OutputException("Problem writing each masked data", ex);
            }
        }
    }
    
    private String dformat(double d)
    {
        if (d != -1)
        {
            return dform.format(d);
        }
        else
        {
            return "------";
        }
    }
    
    private String iformat(int i)
    {
        return String.format("%6d", i);
    }

    /**
     * Get the imputer options config
     * @return The config
     */ 
    public ImmutableNode getConfig()
    {
        ImmutableNode.Builder config = new ImmutableNode.Builder().name("stats");
        
        if (pretty != null)
        {
            config.addChild(new ImmutableNode.Builder().name("pretty").value(pretty.getAbsolutePath()).create());
        }
        if (depth != null)
        {
            config.addChild(new ImmutableNode.Builder().name("depth").value(depth.getAbsolutePath()).create());
        }
        if (geno != null)
        {
            config.addChild(new ImmutableNode.Builder().name("geno").value(geno.getAbsolutePath()).create());
        }
        if (depthGeno != null)
        {
            config.addChild(new ImmutableNode.Builder().name("depthgeno").value(depthGeno.getAbsolutePath()).create());
        }
        if (eachMasked != null)
        {
            config.addChild(new ImmutableNode.Builder().name("eachmasked").value(eachMasked.getAbsolutePath()).create());
        }
        if (partial)
        {
            config.addChild(new ImmutableNode.Builder().name("partial").value("true").create());
        }
        
        return config.create();
    }
    
    private File pretty;
    private File depth;
    private File geno;
    private File depthGeno;
    private File eachMasked;
    private boolean partial;
    
    
    private final DecimalFormat dform = new DecimalFormat("0.0000");
}
