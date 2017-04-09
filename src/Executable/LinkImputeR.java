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

import Accuracy.AccuracyCalculator;
import Accuracy.AccuracyCalculator.AccuracyMethod;
import Accuracy.AccuracyStats;
import Accuracy.DepthMask;
import Accuracy.DepthMask.Method;
import Accuracy.DepthMaskFactory;
import Callers.BinomialCaller;
import Callers.Caller;
import Combiner.Combiner;
import Combiner.MaxDepthCombinerOptimizedCalls;
import Exceptions.ProgrammerException;
import Imputers.Imputer;
import Imputers.KnniLDProbOptimizedCalls;
import Utils.Log;
import Utils.Log.Level;
import Utils.ProbToCall;
import Utils.SingleGenotype.SingleGenotypeCall;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypePosition;
import Utils.SingleGenotype.SingleGenotypeProbability;
import Utils.SingleGenotype.SingleGenotypeReads;
import VCF.ByteToGeno;
import VCF.Filters.MAFFilter;
import VCF.Filters.ParalogHWFilter;
import VCF.Filters.PositionFilter;
import VCF.Filters.PositionMissing;
import VCF.Filters.SampleMissing;
import VCF.Filters.VCFFilter;
import VCF.Genotype;
import VCF.Mappers.DepthMapper;
import VCF.Position;
import VCF.PositionMeta;
import VCF.VCF;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Main class
 * @author Daniel Money
 * @version 0.9
 */
public class LinkImputeR
{
    private LinkImputeR()
    {

    }
    
    /**
     * Main function
     * @param args Command line arguements
     * @throws Exception If an uncaught error occurs
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length == 0)
        {
            //WRITE HELP!
        }
        else
        {
            XMLConfiguration c;
            switch (args[0])
            {
                case "-c":
                    c = convert(new File(args[1]));
                    writeXML(c,new File(args[2]));
                    break;
                case "-s":
                    c = convert(new File(args[1]));
                    accuracy(c);
                    break;
                case "-v":
                    System.out.println("LinkImputeR version 0.9.2");
                    break;
                case "-h":
                    //NEED TO WRITE HELP!
                    break;
                default:
                    File xml = new File(args[0]);

                    FileBasedConfigurationBuilder<XMLConfiguration> builder = 
                        new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
                        .configure(new Parameters().xml().setFile(xml));

                    XMLConfiguration config = builder.getConfiguration();

                    switch (config.getString("mode"))
                    {
                        case "accuracy":
                            accuracy(config);
                            break;
                        case "impute":
                            if (args.length == 3)
                            {
                                impute(config,args[1],new File(args[2]));
                            }
                            else
                            {
                                impute(config,null,null);
                            }
                            break;
                    }
                    break;
            }
        }
    }
    
    private static void impute(XMLConfiguration config, String casename, File output) throws Exception
    {
        long start = System.currentTimeMillis();
        Log.initialise(Level.DEBUG);
        Log.brief("Started " + casename);
        Input input = new Input(config.configurationAt("input"));
        VCF vcf = input.getVCF();
        Log.debug("Data read in");
        
        if (casename == null)
        {
            casename = config.getString("input.case");
        }
        if (output == null)
        {
            output = new File(config.getString("output.filename"));
        }
      
        for (HierarchicalConfiguration<ImmutableNode> caseConfig: config.configurationsAt("case"))
        {
            Case c = new Case(caseConfig);
            if (c.getName().equals(casename))
            {            
                //FILTER
                c.applyFilters(vcf);
                Log.debug("Filters applied");

                //GET READS
                int[][][] readCounts = vcf.asArrayTransposed("AD", new DepthMapper());
                Log.debug("Got reads");

                //CALL            
                Caller caller = c.getCaller();
                double[][][] calledProb = caller.call(readCounts);
                Log.debug("Done calling");

                //IMPUTE
                ProbToCall p2c = new ProbToCall();
                Imputer imputer = c.getImputer();            
                double[][][] imputedProb = imputer.impute(calledProb, readCounts);
                Log.debug("Done imputing");

                //COMBINE          
                Combiner combiner = c.getCombiner();
                double[][][] combinedProb = combiner.combine(calledProb, imputedProb, readCounts);
                byte[][] combinedCalled = p2c.call(combinedProb);
                Log.debug("Done combining");
                
                List<Position> newPositions = new ArrayList<>();
                int i = 0;
                PositionMeta[] old = vcf.getPositions();
                for (PositionMeta pm: old)
                {
                    Position oldp = vcf.singlePosition(pm);
                    byte[] g = new byte[combinedProb.length];
                    double[][] p = new double[combinedProb.length][];
                    for (int j = 0; j < g.length; j++)
                    {
                        g[j] = combinedCalled[j][i];
                        p[j] = combinedProb[j][i];
                    }
                    newPositions.add(makeNewPosition(oldp, g, p));
                    i++;
                }

                List<String> newMeta = vcf.metaStream().collect(Collectors.toList());
                newMeta.add("##FORMAT=<ID=UG,Number=1,Type=String,Description=\"Unimputed Genotype\">");
                newMeta.add("##FORMAT=<ID=IP,Number=3,Type=Float,Description=\"Imputation Probabilities (3 d.p.)\">");

                VCF newVCF = new VCF(newMeta,newPositions);
                newVCF.writeFile(output);
                Log.debug("Output written");
            }
        }
        String time = DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "dd:HH:mm:ss");
        Log.brief("All done\t("+time+")");
    }
    
    private static void accuracy(XMLConfiguration config) throws Exception
    {
        Log.initialise(config.configurationAt("log"));
        long start = System.currentTimeMillis();
        Log.brief("Started");
        List<ImmutableNode> outConfig = new ArrayList<>();
        outConfig.add(new ImmutableNode.Builder().name("mode").value("impute").create());
        
        Input input = new Input(config.configurationAt("input"));
        VCF vcf = input.getVCF();
        outConfig.add(input.getImputeConfig());
        
        Log.brief("Done read in and filter");
                           
        DepthMaskFactory dmf = new DepthMaskFactory(config.configurationAt("mask"));
        
        Output output = new Output(config.configurationAt("output"));
        PrintWriter sum = output.getSummaryWriter();
        PrintWriter table = output.getTableWriter();
        boolean partial = output.getPartial();
        writeSumHeader(sum,partial);        
        
        for (HierarchicalConfiguration<ImmutableNode> caseConfig: config.configurationsAt("case"))
        {
            Case c = new Case(caseConfig);
            Log.brief(c.getName() + ": Starting");
            
            Log.detail(c.getName() + ": Applying filters...");
            //FILTER
            c.applyFilters(vcf);
            
            if ((vcf.numberPositions() > 0) && (vcf.numberSamples()) > 0)
            {
                Log.detail(c.getName() + ": Getting reads...");
                //GET READS
                int[][][] readCounts = vcf.asArrayTransposed("AD", new DepthMapper());

                Log.detail(c.getName() + ": Masking...");
                //MASK
                DepthMask mask = dmf.getDepthMask(readCounts);
                List<SingleGenotypeReads> maskedReads = getMaskedReads(mask.maskedList());

                Log.detail(c.getName() + ": Calling...");
                ProbToCall p2c = new ProbToCall();
                //CALL            
                Caller caller = c.getCaller();
                List<SingleGenotypeProbability> calledProb = 
                    caller.call(maskedReads);
                List<SingleGenotypeCall> calledGeno = p2c.call(calledProb);

                Log.detail(c.getName() + ": Imputing...");
                //IMPUTE
                double[][][] origProb = caller.call(readCounts);
                Imputer imputer = c.getImputer(origProb,readCounts,calledProb,mask.maskedList());            
                List<SingleGenotypeProbability> imputedProb = 
                    imputer.impute(origProb,readCounts,calledProb,mask.maskedList());
                List<SingleGenotypeCall> imputedGeno = p2c.call(imputedProb);

                Log.detail(c.getName() + ": Combining...");
                //COMBINE
                List<SingleGenotypeCall> correctCalls = p2c.call(caller.call(getOriginalReads(mask.maskedList())));            
                Combiner combiner = c.getCombiner(calledProb, imputedProb, maskedReads, correctCalls);

                Log.detail(c.getName() + ": Creating Stats...");
                //STATS
                
                DepthMask testMask = dmf.getDepthMask(readCounts);
                List<SingleGenotypeReads> testMaskedReads = getMaskedReads(testMask.maskedList());
                
                List<SingleGenotypeProbability> testCalledProb = 
                    caller.call(testMaskedReads);
                List<SingleGenotypeCall> testCalledGeno = p2c.call(testCalledProb);
                
                List<SingleGenotypeProbability> testImputedProb = 
                    imputer.impute(origProb,readCounts,testCalledProb,testMask.maskedList());
                List<SingleGenotypeCall> testImputedGeno = p2c.call(testImputedProb);
                
                List<SingleGenotypeCall> testCorrectCalls = p2c.call(caller.call(getOriginalReads(testMask.maskedList())));            
                List<SingleGenotypeProbability> testCombinedProb = combiner.combine(testCalledProb, testImputedProb, maskedReads);
                List<SingleGenotypeCall> testCombinedGeno = p2c.call(testCombinedProb);
                
                List<SingleGenotypeCall> testCorrect = testCorrectCalls;
                AccuracyStats stats = AccuracyCalculator.accuracyStats(testCorrect, testCombinedGeno, testMask.maskedList());
                AccuracyStats cstats = AccuracyCalculator.accuracyStats(testCorrect, testCalledGeno, testMask.maskedList());
                AccuracyStats istats = AccuracyCalculator.accuracyStats(testCorrect, testImputedGeno, testMask.maskedList());
                c.getPrintStats().writeStats(stats, cstats, istats);
                writeSum(sum,c,vcf,stats,cstats,istats,partial);
                writeTable(table,c,vcf,stats,cstats,istats,partial);

        
//                List<SingleGenotypeProbability> combinedProb = combiner.combine(calledProb, imputedProb, maskedReads);
//                List<SingleGenotypeCall> combinedGeno = p2c.call(combinedProb);
//
//                List<SingleGenotypeCall> correct = correctCalls;
//                AccuracyStats stats = AccuracyCalculator.accuracyStats(correct, combinedGeno, mask.maskedList());
//                AccuracyStats cstats = AccuracyCalculator.accuracyStats(correct, calledGeno, mask.maskedList());
//                AccuracyStats istats = AccuracyCalculator.accuracyStats(correct, imputedGeno, mask.maskedList());
//                c.getPrintStats().writeStats(stats, cstats, istats);
//                writeSum(sum,c,vcf,stats,cstats,istats,partial);
//                writeTable(table,c,vcf,stats,cstats,istats,partial);
                
                //ADD IMPUTE CONFIG
                outConfig.add(c.getImputeConfig(caller, imputer, combiner));

                Log.brief(c.getName() + ": Done");
            }
            else
            {
                Log.detail(c.getName() + ": No data left after filtering");
                writeSumError(sum,c,partial);
                Log.brief(c.getName() + ": Done");
            }
            

        }
        
        //WRITE CONFIG
        output.writeControl(outConfig);
        sum.close();
        if (table != null)
        {
            table.close();
        }
        
        String time = DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "dd:HH:mm:ss");
        Log.brief("All done\t("+time+")");
    }
    
    private static XMLConfiguration convert(File ini) throws ConfigurationException, IOException
    {
        FileBasedConfigurationBuilder<INIConfiguration> inibuilder = 
            new FileBasedConfigurationBuilder<>(INIConfiguration.class)
            .configure(new Parameters().fileBased().setFile(ini));
        
        INIConfiguration config = inibuilder.getConfiguration();
        
        List<ImmutableNode> xml = new ArrayList<>();
        
        xml.add(new ImmutableNode.Builder().name("mode").value("accuracy").create());        
        
        //int depth = config.getInt("Global.depth");
        String[] sdepths = config.getString("Global.depth").split(",");
        int[] depths = Arrays.stream(sdepths).mapToInt(s -> Integer.parseInt(s)).toArray();
        int maxDepth = NumberUtils.max(depths);
        int minDepth = NumberUtils.min(depths);
        double error = config.getDouble("Global.error",0.01);
        
        File input = new File(config.getString("Input.filename"));
        List<PositionFilter> inputfilters = new ArrayList<>();
        
        int numSnps = VCF.numberPositionsFromFile(input);
        for (HierarchicalConfiguration<ImmutableNode> i : config.childConfigurationsAt("InputFilters"))
        {
            switch (i.getRootElementName())
            {
                //ADD FILTERS
                case "maf":
                    inputfilters.add(new MAFFilter(i.getDouble(null),maxDepth,100));
                    break;
                case "hw":
                    inputfilters.add(new ParalogHWFilter(error/numSnps,i.getDouble(null)));
                    break;
                case "positionmissing":
                    inputfilters.add(new PositionMissing(i.getDouble(null),minDepth));
                    break;
            }
        }
        
        String saveString = config.getString("Input.save",null);
        File save = (saveString == null) ? null : new File(saveString);
        
        Input o = new Input(input, inputfilters, save);        
        xml.add(o.getConfig());
        
        DepthMaskFactory dmf = new DepthMaskFactory(10000,30,maxDepth,Method.ALL);
        xml.add(dmf.getConfig());
        
        Caller caller = new BinomialCaller(error);
        String statsRoot = config.getString("Stats.root");
        boolean partial = config.getBoolean("Stats.partial",false);
        
        int casenum = 1;
        
        for (int depth: depths)
        {
            List<List<VCFFilter>> cases = new ArrayList<>();        
            cases.add(new ArrayList<>());
            
            for (HierarchicalConfiguration<ImmutableNode> i : config.childConfigurationsAt("CaseFilters"))
            {
                List<List<VCFFilter>> newcases = new ArrayList<>();
                String[] options = i.getString(null).split(",");
                for (String opt: options)
                {
                    VCFFilter f;
                    switch (i.getRootElementName())
                    {
                        case "maf":
                            f = new MAFFilter(Double.parseDouble(opt),depth,100);
                            for (List<VCFFilter> c: cases)
                            {
                                List<VCFFilter> nc = new ArrayList<>(c);
                                nc.add(f);
                                newcases.add(nc);
                            }
                            break;
                        case "missing":
                            VCFFilter fp = new PositionMissing(Double.parseDouble(opt),depth);
                            VCFFilter fs = new SampleMissing(Double.parseDouble(opt),depth);
                            for (List<VCFFilter> c: cases)
                            {
                                List<VCFFilter> nc = new ArrayList<>(c);
                                nc.add(fp);
                                nc.add(fs);
                                newcases.add(nc);
                            }
                            break;
                        case "samplemissing":
                            f = new SampleMissing(Double.parseDouble(opt),depth);
                            for (List<VCFFilter> c: cases)
                            {
                                List<VCFFilter> nc = new ArrayList<>(c);
                                nc.add(f);
                                newcases.add(nc);
                            }
                            break;
                        case "positionmissing":
                            f = new PositionMissing(Double.parseDouble(opt),depth);
                            for (List<VCFFilter> c: cases)
                            {
                                List<VCFFilter> nc = new ArrayList<>(c);
                                nc.add(f);
                                newcases.add(nc);
                            }
                            break;
                        //NEED TO THINK ABOUT WHAT TO DO WITH SIGNIFICANCE!
                        //PSSOBLY CHANGES IN CASE????
                        /*case "hw":
                            f = new ParalogHWFilter(Double.parseDouble(opt),error);
                            for (List<VCFFilter> c: cases)
                            {
                                List<VCFFilter> nc = new ArrayList<>(c);
                                nc.add(f);
                                newcases.add(nc);
                            }
                            break;
                                */
                    }
                }
                cases = newcases;
            }
            
            ImputationOption imputer = new ImputationOption(new KnniLDProbOptimizedCalls(depth,AccuracyMethod.CORRECT));
            CombinerOption combiner = new CombinerOption(new MaxDepthCombinerOptimizedCalls(depth,AccuracyMethod.CORRECT));

            for (List<VCFFilter> filters: cases)
            {
                String name = "Case " + casenum;

                File prettyStats = null;
                File genoStats = null;
                File depthStats = null;
                File dgStats = null;

                switch (config.getString("Stats.level"))
                {
                    case "sum":
                        break;
                    case "pretty":
                        prettyStats = new File(statsRoot + "pretty_" + casenum + ".dat");
                        break;
                    case "table":
                        prettyStats = new File(statsRoot + "pretty_" + casenum + ".dat");
                        genoStats = new File(statsRoot + "geno_" + casenum + ".dat");
                        depthStats = new File(statsRoot + "depth_" + casenum + ".dat");
                        dgStats = new File(statsRoot + "dg_" + casenum + ".dat");                   
                }

                PrintStats print = new PrintStats(prettyStats,genoStats,depthStats,dgStats,partial);

                Case cas = new Case(name,filters,caller,imputer,combiner,print,"Depth(" + Integer.toString(depth) + ")");
                xml.add(cas.getConfig());

                casenum++;
            }
        }
        
        File sum = new File(statsRoot + "sum.dat");
        File control = new File(config.getString("Output.control"));
        File table;
        if (config.getString("Stats.level").equals("table"))
        {
            table = new File(statsRoot + "table.dat");;
        }
        else
        {
            table = null;
        }
        
        Output out = new Output(sum,table,control,partial);
        xml.add(out.getConfig());
        
        File log;
        String ls = config.getString("Log.file",null);
        if (ls != null)
        {
            log = new File(ls);
        }
        else
        {
            log = null;
        }
        Level level;
        switch(config.getString("Log.level","critical").toLowerCase())
        {
            case "brief":
                level = Log.Level.BRIEF;
                break;
            case "detai":
                level = Log.Level.DETAIL;
                break;
            case "debug":
                level = Log.Level.DEBUG;
                break;
            default:
                level = Log.Level.CRITICAL;
                break;
        }

        xml.add(Log.getConfig(level,log));
       
        BasicConfigurationBuilder<ExtendedXMLConfiguration> xmlbuilder = 
            new BasicConfigurationBuilder<>(ExtendedXMLConfiguration.class)
            .configure(new Parameters().xml());
        
        ExtendedXMLConfiguration c;
        try
        {
            c = xmlbuilder.getConfiguration();
        }
        catch (ConfigurationException ex)
        {
            throw new ProgrammerException(ex);
        }
        
        c.setRootElementName("linkimputepro");
        
        c.addNodes(null, xml);
        
        return c;
    }
    
    private static void writeXML(XMLConfiguration xml, File output) throws IOException
    {
        FileHandler handler = new FileHandler(xml);
        try
        {
            handler.save(output);
        }
        catch (ConfigurationException ex)
        {
            if (ex.getCause() instanceof IOException)
            {
                IOException tex = (IOException) ex.getCause();
                throw tex;
            }
            throw new ProgrammerException(ex);
        }
    }
    
    private static Position makeNewPosition(Position original, byte[] newGeno, double[][] newProbs)
    {
        StringBuilder newFormat = new StringBuilder();
        for (String f: original.meta().getFormat())
        {
            newFormat.append(f);
            newFormat.append(":");
        }
        newFormat.append("OG");
        newFormat.append(":");
        newFormat.append("IP");
        
        LinkedHashMap<String,String> genotypes = new LinkedHashMap<>();
        Genotype[] o = original.genotypeStream().toArray(i -> new Genotype[i]);
        
        for (int i = 0; i < o.length; i++)
        {
            genotypes.put(o[i].getSampleName(), makeNewGenotype(o[i],newGeno[i],newProbs[i]));
        }
        
        String[] pm = new String[9];
        pm[0] = original.meta().getChrom();
        pm[1] = original.meta().getPosition();
        pm[2] = original.meta().getID();
        pm[3] = original.meta().getRef();
        StringBuilder alt = new StringBuilder();
        for (String a: original.meta().getAlt())
        {
            alt.append(a);
            alt.append(",");
        }
        pm[4] = alt.substring(0, alt.length() - 1);
        pm[5] = original.meta().getQual();
        pm[6] = original.meta().getFilter();
        pm[7] = original.meta().getInfo();
        pm[8] = newFormat.toString();
        
        return new Position(new PositionMeta(pm),genotypes);
    }
    
    private static String makeNewGenotype(Genotype original, byte newGeno, double[] probs)
    {
        //As we don't ever use the original genotype we should probably check it exists first!
        String oldGeno = original.getData("GT");
        Genotype temp = original.copy();
        temp.replaceData("GT", b2g.map(newGeno));
        StringBuilder newString = new StringBuilder();
        newString.append(temp.getData());
        newString.append(":");
        newString.append(oldGeno);
        newString.append(":");
        newString.append(dform.format(probs[0]));
        newString.append(",");
        newString.append(dform.format(probs[1]));
        newString.append(",");
        newString.append(dform.format(probs[2]));
        return newString.toString();
    }
    
    private static void writeSum(PrintWriter sum, Case c, VCF vcf, AccuracyStats stats, AccuracyStats cstats, AccuracyStats istats, boolean partial)
    {
        sum.print(c.getName());
        sum.print("\t");
        sum.print(vcf.getSamples().length);
        sum.print("\t");
        sum.print(vcf.getPositions().length);
        sum.print("\t");
        sum.print(dforms.format(stats.accuracy()));
        sum.print("\t");
        sum.print(dforms.format(stats.correlation()));
        sum.print("\t");
        sum.print(c.getFilterSummary());
        sum.print("\t");
        sum.print(c.getAdditional());
        
        if (partial)
        {
            sum.print("\t[");
            sum.print(dforms.format(cstats.accuracy()));
            sum.print("/");
            sum.print(dforms.format(istats.accuracy()));
            sum.print("]");
            
            sum.print("\t[");
            sum.print(dforms.format(cstats.correlation()));
            sum.print("/");
            sum.print(dforms.format(istats.correlation()));
            sum.print("]");
        }
        
        sum.println();
        sum.flush();
    }
    
    private static void writeSumError(PrintWriter sum, Case c, boolean partial)
    {
        sum.print(c.getName());
        sum.print("\t");
        sum.print(0);
        sum.print("\t");
        sum.print(0);
        sum.print("\t");
        sum.print(dforms.format(0.0));
        sum.print("\t");
        sum.print(dforms.format(0.0));
        sum.print("\t");
        sum.print(c.getFilterSummary());
        sum.print("\t");
        sum.print(c.getAdditional());
        
        if (partial)
        {
            sum.print("\t[");
            sum.print(dforms.format(0.0));
            sum.print("/");
            sum.print(dforms.format(0.0));
            sum.print("]");
            
            sum.print("\t[");
            sum.print(dforms.format(0.0));
            sum.print("/");
            sum.print(dforms.format(0.0));
            sum.print("]");
        }
        
        sum.println();
        sum.flush();
    }
    
    private static void writeTable(PrintWriter table, Case c, VCF vcf, AccuracyStats stats, AccuracyStats cstats, AccuracyStats istats, boolean partial)
    {
        if (table != null)
        {
            table.print(c.getName());
            table.print("\t");
            table.print(vcf.getSamples().length);
            table.print("\t");
            table.print(vcf.getPositions().length);
            table.print("\t");
            table.print(dforms.format(stats.accuracy()));

            if (partial)
            {
                table.print("\t");
                table.print(dforms.format(cstats.accuracy()));
                table.print("\t");
                table.print(dforms.format(istats.accuracy()));
            }
            
            table.print("\t");
            table.print(dforms.format(stats.correlation()));

            if (partial)
            {
                table.print("\t");
                table.print(dforms.format(cstats.correlation()));
                table.print("\t");
                table.print(dforms.format(istats.correlation()));
            }

            table.println();
            table.flush();
        }
    }
    
    private static void writeSumHeader(PrintWriter sum, boolean partial)
    {
        sum.print("Name\tSamples\tPositions\tAccuracy\tCorrelation\tFilters\tAdditional");
        if (partial)
        {
            sum.print("\tPartials (Accuracy)\tParials (Correlation)");
        }
        sum.println();
    }
    
    private static List<SingleGenotypeCall> getCorrectCalls(double[][][] called, List<? extends SingleGenotypePosition> list)
    {
        ProbToCall p2c = new ProbToCall();
        return list.stream().map(sgp ->
            new SingleGenotypeCall(sgp.getSample(),sgp.getSNP(),p2c.callSingle(called[sgp.getSample()][sgp.getSNP()])))
            .collect(Collectors.toCollection(ArrayList::new));
    }
    
    private static List<SingleGenotypeReads> getMaskedReads(List<SingleGenotypeMasked> masked)
    {
        return masked.stream().map(sgm -> 
                new SingleGenotypeReads(sgm.getSample(), sgm.getSNP(), sgm.getMasked()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    private static List<SingleGenotypeReads> getOriginalReads(List<SingleGenotypeMasked> masked)
    {
        return masked.stream().map(sgm -> 
                new SingleGenotypeReads(sgm.getSample(), sgm.getSNP(), sgm.getOriginal()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    private final static DecimalFormat dform = new DecimalFormat("0.000");
    private final static DecimalFormat dforms = new DecimalFormat("0.0000");
    private final static ByteToGeno b2g = new ByteToGeno();
}
