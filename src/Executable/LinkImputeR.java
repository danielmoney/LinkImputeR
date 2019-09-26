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
import Exceptions.*;
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
import VCF.Exceptions.VCFException;
import VCF.Exceptions.VCFInputException;
import VCF.Exceptions.VCFNoDataException;
import VCF.Filters.MAFFilter;
import VCF.Filters.ParalogHWFilter;
import VCF.Filters.PositionFilter;
import VCF.Filters.PositionMissing;
import VCF.Filters.SampleMissing;
import VCF.Filters.VCFFilter;
import VCF.Genotype;
import VCF.Mappers.DepthMapper;
import VCF.Meta;
import VCF.Position;
import VCF.PositionMeta;
import VCF.VCF;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import org.apache.commons.cli.*;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Main class
 * @author Daniel Money
 * @version 1.1.4
 */
public class LinkImputeR
{
    private LinkImputeR()
    {

    }
    
    /**
     * Main function
     * @param args Command line arguments
     * @throws Exception If an uncaught error occurs
     */
    public static void main(String[] args) throws Exception
    {
        try
        {
            Options options = new Options();

            OptionGroup all = new OptionGroup();
            all.addOption(Option.builder("c").build());
            all.addOption(Option.builder("s").build());
            all.addOption(Option.builder("v").build());
            all.addOption(Option.builder("h").build());
            options.addOptionGroup(all);

            CommandLineParser parser = new DefaultParser();
            CommandLine commands = parser.parse(options,args);

            String[] fileNames = commands.getArgs();

            XMLConfiguration c;
            boolean done = false;

            if (commands.hasOption("c"))
            {
                if (fileNames.length == 2)
                {
                    c = convert(new File(fileNames[0]));
                    writeXML(c, new File(fileNames[1]));
                }
                else
                {
                    System.out.println("An input and output file must be provided");
                    System.out.println();
                    help();
                }
                done = true;
            }

            if (commands.hasOption("s"))
            {
                if (fileNames.length == 1)
                {
                    c = convert(new File(fileNames[0]));
                    accuracy(c);
                }
                else
                {
                    System.out.println("An input file must be provided");
                    System.out.println();
                    help();
                }
                done = true;
            }

            if (commands.hasOption("v"))
            {
                System.out.println("LinkImputeR version 1.1.4");
                done = true;
            }

            if (commands.hasOption("h"))
            {
                help();
                done = true;
            }

            if (!done)
            {
                if (fileNames.length == 3)
                {
                    File xml = new File(fileNames[0]);

                    FileBasedConfigurationBuilder<XMLConfiguration> builder =
                            new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
                                    .configure(new Parameters().xml().setFile(xml));

                    XMLConfiguration config;
                    try
                    {
                        config = builder.getConfiguration();
                    }
                    catch (ConfigurationException ex)
                    {
                        throw new XMLException("There's a problem reading the xml file.  "
                                + "Does it exist? Is it formatted correctly?",ex);
                    }

                    switch (config.getString("mode"))
                    {
                        case "accuracy":
                            accuracy(config);
                            break;
                        case "impute":
                            if (args.length == 3)
                            {
                                impute(config, args[1], new File(args[2]));
                            }
                            else
                            {
                                impute(config, null, null);
                            }
                            break;
                    }
                }
                else
                {
                    System.out.println("An input file, case name and output file must be provided (in that order)");
                    System.out.println();
                    help();
                }
            }
        }
        catch (UnrecognizedOptionException ex)
        {
            System.err.println("Unrecognised command line option (" + ex.getOption() + ")");
            System.err.println();
            help();
        }
        catch (AlreadySelectedException ex)
        {
            System.err.println("Only one option can be selected at a time");
            System.err.println();
            help();
        }
        catch (VCFException ex)
        {
            System.err.println("=====");
            System.err.println("ERROR");
            System.err.println("=====");
            System.err.println("There's a problem with the VCF file");
            System.err.println(ex.getMessage());
            System.err.println();
            System.err.println("Technical details follow:");
            throw ex;
        }
        catch (INIException ex)
        {
            System.err.println("=====");
            System.err.println("ERROR");
            System.err.println("=====");
            System.err.println("There's a problem with the ini file");
            System.err.println(ex.getMessage());
            System.err.println();
            System.err.println("Technical details follow:");
            throw ex;
        }
        catch (OutputException ex)
        {
            System.err.println("=====");
            System.err.println("ERROR");
            System.err.println("=====");
            System.err.println("There's a problem writing an output file");
            System.err.println(ex.getMessage());
            System.err.println();
            System.err.println("Technical details follow:");
            throw ex;
        }
        catch (AlgorithmException ex)
        {
            System.err.println("=====");
            System.err.println("ERROR");
            System.err.println("=====");
            System.err.println("There's a problem with the algorithms");
            System.err.println(ex.getMessage());
            System.err.println();
            System.err.println("Technical details follow:");
            throw ex;
        }
        catch (ProgrammerException ex)
        {
            System.err.println("=====");
            System.err.println("ERROR");
            System.err.println("=====");
            System.err.println("Well this is embarrassing.  This shouldn't have happened.  "
                    + "Please contact the maintainer if you can not solve the error"
                    + "from the technical details.");
            System.err.println();
            System.err.println("Technical details follow:");
            throw ex;            
        }
        catch (Exception ex)
        {
            System.err.println("=====");
            System.err.println("ERROR");
            System.err.println("=====");
            System.err.println("Well this is embarrassing.  This was not expected to have happened.  "
                    + "Please contact the maintainer if you can not solve the error"
                    + "from the technical details.");
            System.err.println();
            System.err.println("Note: The maintainer would be interested in knowing "
                    + "about any XML related messages so he can write nicer error "
                    + "messages for these problems.");
            System.err.println();
            System.err.println("Technical details follow:");
            throw ex;            
        }
    }
    
    private static void impute(XMLConfiguration config, String casename, File output) throws VCFException, OutputException
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

                List<String> newMeta = vcf.getMeta().getLinesList();
                newMeta.add("##FORMAT=<ID=UG,Number=1,Type=String,Description=\"Unimputed Genotype\">");
                newMeta.add("##FORMAT=<ID=IP,Number=3,Type=Float,Description=\"Imputation Probabilities (3 d.p.)\">");

                VCF newVCF = new VCF(new Meta(newMeta),newPositions);
                try
                {
                    newVCF.writeFile(output);
                }
                catch (IOException ex)
                {
                    throw new OutputException("Problem writing the imputed VCF");
                }
                Log.debug("Output written");
            }
        }
        String time = DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "dd:HH:mm:ss");
        Log.brief("All done\t("+time+")");
    }
    
    private static void accuracy(XMLConfiguration config) throws VCFException, OutputException, AlgorithmException
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

                Caller caller = c.getCaller();
                Log.detail(c.getName() + ": Masking...");
                //MASK
                DepthMask mask = dmf.getDepthMask(readCounts,caller);
                List<SingleGenotypeReads> maskedReads = getMaskedReads(mask.maskedList());

                Log.detail(c.getName() + ": Calling...");
                ProbToCall p2c = new ProbToCall();
                //CALL            
                List<SingleGenotypeProbability> calledProb = 
                    caller.call(maskedReads);

                Log.detail(c.getName() + ": Imputing...");
                //IMPUTE
                double[][][] origProb = caller.call(readCounts);
                Imputer imputer = c.getImputer(origProb,readCounts,calledProb,mask.maskedList());            

                
                Log.detail(c.getName() + ": Combining...");

                DepthMask validateMask = dmf.getDepthMask(readCounts,mask.maskedPositions(),caller);
                List<SingleGenotypeReads> validateMaskedReads = getMaskedReads(validateMask.maskedList());

                List<SingleGenotypeProbability> validateCalledProb = 
                    caller.call(validateMaskedReads);

                List<SingleGenotypeProbability> validateImputedProb = 
                    imputer.impute(origProb,readCounts,validateCalledProb,validateMask.maskedList());


                //COMBINE
                List<SingleGenotypeCall> validateCorrectCalls = p2c.call(caller.call(getOriginalReads(validateMask.maskedList())));            
                Combiner combiner = c.getCombiner(validateCalledProb, validateImputedProb, validateMaskedReads, validateCorrectCalls, validateMask.maskedList());
                    
                Log.detail(c.getName() + ": Creating Stats...");
                //STATS
                
                List<SingleGenotypePosition> ignoredPositions = new ArrayList<>();
                ignoredPositions.addAll(mask.maskedPositions());
                ignoredPositions.addAll(validateMask.maskedPositions());
                DepthMask testMask = dmf.getDepthMask(readCounts,ignoredPositions,caller);
                List<SingleGenotypeReads> testMaskedReads = getMaskedReads(testMask.maskedList());
                
                List<SingleGenotypeProbability> testCalledProb = 
                    caller.call(testMaskedReads);
                List<SingleGenotypeCall> testCalledGeno = p2c.call(testCalledProb);
                
                List<SingleGenotypeProbability> testImputedProb = 
                    imputer.impute(origProb,readCounts,testCalledProb,testMask.maskedList());
                List<SingleGenotypeCall> testImputedGeno = p2c.call(testImputedProb);
                
                List<SingleGenotypeCall> testCorrectCalls = p2c.call(caller.call(getOriginalReads(testMask.maskedList())));            
                List<SingleGenotypeProbability> testCombinedProb = combiner.combine(testCalledProb, testImputedProb, testMaskedReads);
                List<SingleGenotypeCall> testCombinedGeno = p2c.call(testCombinedProb);

                AccuracyStats stats = AccuracyCalculator.accuracyStats(testCorrectCalls, testCombinedGeno, testMask.maskedList());
                AccuracyStats cstats = AccuracyCalculator.accuracyStats(testCorrectCalls, testCalledGeno, testMask.maskedList());
                AccuracyStats istats = AccuracyCalculator.accuracyStats(testCorrectCalls, testImputedGeno, testMask.maskedList());
                c.getPrintStats().writeStats(stats, cstats, istats);
                writeSum(sum,c,vcf,stats,cstats,istats,partial);
                writeTable(table,c,vcf,stats,cstats,istats,partial);

                c.getPrintStats().writeEachMasked(testCorrectCalls,testCombinedGeno,vcf.getSamples(),vcf.getPositions());
          
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
    
    private static XMLConfiguration convert(File ini) throws INIException, VCFInputException
    {
        FileBasedConfigurationBuilder<INIConfiguration> inibuilder =
            new FileBasedConfigurationBuilder<>(INIConfiguration.class)
            .configure(new Parameters().fileBased().setFile(ini));
        
        INIConfiguration config;
        try
        {
            config = inibuilder.getConfiguration();
        }
        catch (ConfigurationException ex)
        {
            throw new INIException("There's a problem reading the ini file.  "
                    + "Does it exist? Is it formatted correctly?",ex);
        }
        
        List<ImmutableNode> xml = new ArrayList<>();
        
        xml.add(new ImmutableNode.Builder().name("mode").value("accuracy").create());        
        
        String[] sdepths = config.getString("Global.depth").split(",");
        int[] depths = new int[sdepths.length];
        for (int i = 0; i < sdepths.length; i++)
        {
            try
            {
                depths[i] = Integer.parseInt(sdepths[i]);
            }
            catch (NumberFormatException ex)
            {
                throw new INIException("Values for the depth option must be integers");
            }
            
            if (depths[i] <= 0)
            {
                throw new INIException("Values for the depth option must be positive");
            }
                    
        }
        
        int maxDepth = NumberUtils.max(depths);
        int minDepth = NumberUtils.min(depths);
        double error;
        try
        {
            error = config.getDouble("Global.error",0.01);
        }
        catch (ConversionException ex)
        {
            throw new INIException("Values for the error option must be a number");
        }
        
        if ((error <= 0.0) || (error > 1.0))
        {
            throw new INIException("Values for error must be between 0 and 1");
        }
        
        File input = new File(config.getString("Input.filename"));
        List<PositionFilter> inputfilters = new ArrayList<>();
        
        int numSnps = VCF.numberPositionsFromFile(input);
        for (HierarchicalConfiguration<ImmutableNode> i : config.childConfigurationsAt("InputFilters"))
        {
                switch (i.getRootElementName())
                {
                    //ADD FILTERS
                    case "maf":
                        double maf;
                        try
                        {
                            maf = i.getDouble(null);
                        }
                        catch (ConversionException ex)
                        {
                            throw new INIException("Parameter for the MAF Filter must be a number");
                        }
                        if ((maf <= 0) | (maf >= 0.5))
                        {
                            throw new INIException("Parameter for the MAF filter must be between 0 and 0.5");
                        }
                        inputfilters.add(new MAFFilter(maf,8,100,error));
                        break;
                    case "hw":
                        double sig;
                        try
                        {
                            sig = i.getDouble(null);
                        }
                        catch (ConversionException ex)
                        {
                            throw new INIException("Parameter for the HW Filter must be a number");
                        }
                        if ((sig <= 0) | (sig >= 1.0))
                        {
                            throw new INIException("Parameter for the HWS filter must be between 0 and 1");
                        }
                        inputfilters.add(new ParalogHWFilter(sig/numSnps,error));
                        break;
                    case "positionmissing":
                        double missing;
                        try
                        {
                            missing = i.getDouble(null);
                        }
                        catch (ConversionException ex)
                        {
                            throw new INIException("Parameter for the Position Missing filter must be a number");
                        }
                        if ((missing <= 0) | (missing >= 1.0))
                        {
                            throw new INIException("Parameter for the Position Missing filter must be between 0 and 1");
                        }
                        inputfilters.add(new PositionMissing(i.getDouble(null),minDepth));
                        break;
                }
        }
        
        String saveString = config.getString("Input.save",null);
        File save = (saveString == null) ? null : new File(saveString);
        
        String readsformat = config.getString("Input.readsformat",null);
        if ((readsformat != null) && (readsformat.split(",").length > 2))
        {
            throw new INIException("readsformat must be either a single format or two"
                    + "formats comma separated (LinkImputeR currently only works on"
                    + "biallelic SNPs)");
        }
        
        int maxInDepth;
        try
        {
            maxInDepth = config.getInt("Input.maxdepth",100);
        }
        catch (ConversionException ex)
        {
            throw new INIException("Parameter values for the maxdepth option must be an integer.");
        }
        
        Input o = new Input(input, inputfilters, save, maxInDepth, readsformat);        
        xml.add(o.getConfig());
        
        String sampleMethod = config.getString("Accuracy.maskmethod","all");
        Method sm;
        switch (sampleMethod)
        {
            case "bysnp":
                sm = Method.BYSNP;
                break;
            case "bysample":
                sm = Method.BYSAMPLE;
                break;
            case "all":
                sm = Method.ALL;
                break;
            default:
                throw new INIException("maskmethod must be either \"bysnp\", \"bysample\" or \"all\".");
        }
        int numberMasked;
        try
        {
            numberMasked = config.getInt("Accuracy.numbermasked", 10000);
        }
        catch (ConversionException ex)
        {
            throw new INIException("Parameter values for the numbermasked option must be an integer.");
        }
        int minMaskDepth;
        try
        {
            minMaskDepth = config.getInt("Accuracy.mindepth", 30);
        }
        catch (ConversionException ex)
        {
            throw new INIException("Parameter values for the maxdepth option must be an integer");
        }
        DepthMaskFactory dmf = new DepthMaskFactory(numberMasked,minMaskDepth,maxDepth,sm);
        xml.add(dmf.getConfig());
        
        String accuracyMethod = config.getString("Accuracy.accuracymethod","correct");
        AccuracyMethod am;
        switch (accuracyMethod)
        {
            case "correlation":
                am = AccuracyMethod.CORRELATION;
                break;
            case "correct":
                am = AccuracyMethod.CORRECT;
                break;
            default:
                throw new INIException("accuractymethod must be either \"correlation\" or \"correct\".");
        }
        
        Caller caller = new BinomialCaller(error);
        String statsRoot = config.getString("Stats.root");
        boolean partial;
        try
        {
            partial = config.getBoolean("Stats.partial",false);
        }
        catch (ConversionException ex)
        {
            throw new INIException("Stats partial must be convertible to a boolean.  Try \"yes\" or \"no\".");
        }

        boolean eachMasked;
        try
        {
            eachMasked = config.getBoolean("Stats.eachmasked",false);
        }
        catch (ConversionException ex)
        {
            throw new INIException("Stats eachmasked must be convertible to a boolean.  Try \"yes\" or \"no\".");
        }
        
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
                            double maf;
                            try
                            {
                                maf = Double.parseDouble(opt);
                            }
                            catch (NumberFormatException ex)
                            {
                                throw new INIException("Parameter for the MAF Filter must be a number");
                            }
                            if ((maf <= 0) | (maf >= 0.5))
                            {
                                throw new INIException("Parameter for the MAF filter must be between 0 and 0.5");
                            }
                            f = new MAFFilter(maf,8,100,error);
                            for (List<VCFFilter> c: cases)
                            {
                                List<VCFFilter> nc = new ArrayList<>(c);
                                nc.add(f);
                                newcases.add(nc);
                            }
                            break;
                        case "missing":
                            double missing;
                            try
                            {
                                missing = Double.parseDouble(opt);
                            }
                            catch (NumberFormatException ex)
                            {
                                throw new INIException("Parameter for the Missing filter must be a number");
                            }
                            if ((missing <= 0) | (missing >= 1.0))
                            {
                                throw new INIException("Parameter for the Missing filter must be between 0 and 1");
                            }
                            
                            VCFFilter fp = new PositionMissing(missing,depth);
                            VCFFilter fs = new SampleMissing(missing,depth);
                            for (List<VCFFilter> c: cases)
                            {
                                List<VCFFilter> nc = new ArrayList<>(c);
                                nc.add(fp);
                                nc.add(fs);
                                newcases.add(nc);
                            }
                            break;
                        case "samplemissing":
                            double smissing;
                            try
                            {
                                smissing = Double.parseDouble(opt);
                            }
                            catch (NumberFormatException ex)
                            {
                                throw new INIException("Parameter for the Missing filter must be a number");
                            }
                            if ((smissing <= 0) | (smissing >= 1.0))
                            {
                                throw new INIException("Parameter for the Missing filter must be between 0 and 1");
                            }
                            f = new SampleMissing(smissing,depth);
                            for (List<VCFFilter> c: cases)
                            {
                                List<VCFFilter> nc = new ArrayList<>(c);
                                nc.add(f);
                                newcases.add(nc);
                            }
                            break;
                        case "positionmissing":
                            double pmissing;
                            try
                            {
                                pmissing = Double.parseDouble(opt);
                            }
                            catch (NumberFormatException ex)
                            {
                                throw new INIException("Parameter for the Missing filter must be a number");
                            }
                            if ((pmissing <= 0) | (pmissing >= 1.0))
                            {
                                throw new INIException("Parameter for the Missing filter must be between 0 and 1");
                            }
                            f = new PositionMissing(pmissing,depth);
                            for (List<VCFFilter> c: cases)
                            {
                                List<VCFFilter> nc = new ArrayList<>(c);
                                nc.add(f);
                                newcases.add(nc);
                            }
                            break;
                        //NEED TO THINK ABOUT WHAT TO DO WITH SIGNIFICANCE!
                        //POSSIBLY CHANGES IN CASE????
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
                //Catch the case where we have no case filters and add a case with no filters
                if (cases.size() == 0)
                {
                    cases.add(new ArrayList<VCFFilter>());
                }
            }
            
            ImputationOption imputer = new ImputationOption(new KnniLDProbOptimizedCalls(depth,am));
            CombinerOption combiner = new CombinerOption(new MaxDepthCombinerOptimizedCalls(depth,am));

            for (List<VCFFilter> filters: cases)
            {
                String name = "Case " + casenum;

                File prettyStats = null;
                File genoStats = null;
                File depthStats = null;
                File dgStats = null;
                File eachMaskedFile = null;

                switch (config.getString("Stats.level", "sum"))
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
                        break;
                    default:
                        throw new INIException("Stats level must be either \"sum\", \"pretty\" or \"table\".");
                }

                if (eachMasked)
                {
                    eachMaskedFile = new File(statsRoot + "each_" + casenum + ".dat");
                }

                PrintStats print = new PrintStats(prettyStats,genoStats,depthStats,dgStats,eachMaskedFile,partial);

                Case cas = new Case(name,filters,caller,imputer,combiner,print,"Depth(" + depth + ")");
                xml.add(cas.getConfig());

                casenum++;
            }
        }
        
        File sum = new File(statsRoot + "sum.dat");
        File control = new File(config.getString("Output.control"));
        File table;
        if (config.getString("Stats.level").equals("table"))
        {
            table = new File(statsRoot + "table.dat");
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
            case "detail":
                level = Log.Level.DETAIL;
                break;
            case "debug":
                level = Log.Level.DEBUG;
                break;
            case "critical":
                level = Log.Level.CRITICAL;
                break;
            default:
                throw new INIException("log level must be either \"critical\", \"brief\", \"details\" or \"debug\".");
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
        
        c.setRootElementName("linkimputer");
        
        c.addNodes(null, xml);
        
        return c;
    }
    
    private static void writeXML(XMLConfiguration xml, File output) throws OutputException
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
                throw new OutputException("Problem writing XML file",tex);
            }
            throw new ProgrammerException(ex);
        }
    }
    
    private static Position makeNewPosition(Position original, byte[] newGeno, double[][] newProbs) throws VCFNoDataException
    {
        StringBuilder newFormat = new StringBuilder();
        for (String f: original.meta().getFormat())
        {
            newFormat.append(f);
            newFormat.append(":");
        }
        newFormat.append("UG");
        newFormat.append(":");
        newFormat.append("IP");
        
        LinkedHashMap<String,String> genotypes = new LinkedHashMap<>();
        Genotype[] o = original.genotypeStream().toArray((IntFunction<Genotype[]>) Genotype[]::new);
        
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
    
    private static String makeNewGenotype(Genotype original, byte newGeno, double[] probs) throws VCFNoDataException
    {
        //As we don't ever use the original genotype we should probably check it exists first!
        String oldGeno = original.getData("GT");
        Genotype temp = original.copy();
        temp.replaceData("GT", b2g.map(newGeno));
        String newString = temp.getData() +
                ":" +
                oldGeno +
                ":" +
                dform.format(probs[0]) +
                "," +
                dform.format(probs[1]) +
                "," +
                dform.format(probs[2]);
        return newString;
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
            sum.print("\tPartials (Accuracy)\tPartials (Correlation)");
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
    
    private static void help()
    {
        System.out.println("Using LinkImputer");
        System.out.println("\tLinkImputeR has several different ways it can be called.");
        System.out.println("\tThese are outlined below.");
        System.out.println("\tPlease see the user manual for more details and examples.");
        System.out.println();
        System.out.println();
        System.out.println("Accuracy calculation (simple)");
        System.out.println("\tjava -jar LinkImputeR.jar -s filename");
        System.out.println();
        System.out.println("\twhere:");
        System.out.println("\t\tfilename is the name of the ini file");
        System.out.println();
        System.out.println("Accuracy calculation (advanced)");
        System.out.println("\tjava -jar LinkImputeR.jar filename");
        System.out.println();
        System.out.println("\twhere:");
        System.out.println("\t\tfilename is the name of the xml file");
        System.out.println();
        System.out.println("Accuracy ini to xml conversion");
        System.out.println("\tjava -jar LinkImputeR.jar -c inifilename xmlfilename");
        System.out.println();
        System.out.println("\twhere:");
        System.out.println("\t\tinifilename is the name of the ini input file");
        System.out.println("\t\txmlfilename is the name of the xml output file");
        System.out.println();
        System.out.println("Imputation");
        System.out.println("\tjava -jar LinkImputeR.jar xmlfilename case vcffilename");
        System.out.println();
        System.out.println("\twhere:");
        System.out.println("\t\txmlfilename is the name of the xml input file");
        System.out.println("\t\tcase is the name of the case to be used for imputation");
        System.out.println("\t\tvcffilename is the name of the vcf output file");
        System.out.println();
        System.out.println("Version information");
        System.out.println("\tjava -jar LinkImputeR.jar -v");
        System.out.println();
        System.out.println("Help");
        System.out.println("\tjava -jar LinkImputeR.jar -h");
    }
    
    private final static DecimalFormat dform = new DecimalFormat("0.000");
    private final static DecimalFormat dforms = new DecimalFormat("0.0000");
    private final static ByteToGeno b2g = new ByteToGeno();
}
