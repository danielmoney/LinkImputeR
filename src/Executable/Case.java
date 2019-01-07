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

import Callers.Caller;
import Combiner.Combiner;
import Imputers.Imputer;
import Utils.SingleGenotype.SingleGenotypeCall;
import Utils.SingleGenotype.SingleGenotypeMasked;
import Utils.SingleGenotype.SingleGenotypeProbability;
import Utils.SingleGenotype.SingleGenotypeReads;
import VCF.Exceptions.VCFDataException;
import VCF.Filters.VCFFilter;
import VCF.VCF;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Represent the LinkImputeR inputs for a single case (filters, caller,
 * imputer and combiner)
 * @author Daniel Money
 * @version 0.9
 */
public class Case
{

    /**
     * Constructor
     * @param name The name of the case
     * @param filters The filters to be applied
     * @param caller The caller to use
     * @param imputer The imputer options
     * @param combiner The combiner options
     * @param print The stats printing options
     * @param additional Additional string to be used in output (currently
     * used to show minimum read depth)
     */
    public Case(String name, List<VCFFilter> filters, Caller caller, 
            ImputationOption imputer, CombinerOption combiner,
            PrintStats print, String additional)
    {
        this.name = name;
        this.filters = filters;        
        this.caller = caller;
        this.imputer = imputer;
        this.combiner = combiner;
        this.print = print;
        this.additional = additional;
    }
    
    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public Case(HierarchicalConfiguration<ImmutableNode> params)
    {
        name = params.getString("name");
        filters = new ArrayList<>();
        
        for (HierarchicalConfiguration<ImmutableNode> filter: params.configurationsAt("filter"))
        {
            filters.add(Available.getFilter(filter));
        }
        
        caller = Available.getCaller(params.configurationAt("caller"));
        
        imputer = new ImputationOption(params.configurationAt("imputation"));
        
        combiner = new CombinerOption(params.configurationAt("combiner"));        
        
        //THIS IS ONE HELL OF A FUDGE ANDS SHOULD BE FIXED!
        if (params.containsKey("stats.pretty") || params.containsKey("stats.depth")
                || params.containsKey("stats.geno") || params.containsKey("stats.depthgeno"))
        {
            print = new PrintStats(params.configurationAt("stats"));
        }
        
        additional = params.getString("additional", "");
    }
            
    /**
     * Apply the filters of this case to a VCF
     * @param vcf The VCF
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public void applyFilters(VCF vcf) throws VCFDataException
    {
        vcf.resetVisible();
        for (VCFFilter f: filters)
        {
            f.change(vcf);
        }
    }
    
    /**
     * Get the caller for this case
     * @return The caller
     */
    public Caller getCaller()
    {
        return caller;
    }
    
    /**
     * Get the imputer for this case.  If the case imputer is optimizable return
     * an optimized version else return the imputer
     * @param original The called genotype probabilities
     * @param readCounts The read counts
     * @param maskedprobs The masked genotype probabilities
     * @param list List of masked positions
     * @return The imputer
     */
    public Imputer getImputer(double[][][] original, int[][][] readCounts, List<SingleGenotypeProbability> maskedprobs,
                              List<SingleGenotypeMasked> list)
    {
        return imputer.getImputer(original, readCounts, maskedprobs, list);
    }
    
    /**
     * Get the imputer for this case if it doesn't require optimizing, else throw
     * an error
     * @return The imputer
     */
    public Imputer getImputer()
    {
        return imputer.getImputer();
    }
    
    /**
     * Get the combiner for this case.  If the case combiner is optimizable return
     * an optimized version else return the imputer
     * @param called The called genotype probabilities
     * @param imputed The imputed genotype probabilities
     * @param reads The read counts
     * @param correct The correct genotypes
     * @param masked A list of masked genotypes
     * @return The combiner
     */
    public Combiner getCombiner(List<SingleGenotypeProbability> called,
            List<SingleGenotypeProbability> imputed,
            List<SingleGenotypeReads> reads,
            List<SingleGenotypeCall> correct,
            List<SingleGenotypeMasked> masked)
    {
        return combiner.getCombiner(called, imputed, reads, correct, masked);
    }
    
    /**
     * Get the combiner for this case if it doesn't require optimizing, else throw
     * an error
     * @return The combiner
     */
    public Combiner getCombiner()
    {
        return combiner.getCombiner();
    }
    
    /**
     * Get the PrintStats object for this case
     * @return The print stats object
     */
    public PrintStats getPrintStats()
    {
        return print;
    }
    
    /**
     * Get the name of this case
     * @return The name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Get a summary of filters used in this case
     * @return The summary
     */
    public String getFilterSummary()
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (VCFFilter f: filters)
        {
            if (!first)
            {
                sb.append(",");
            }
            else
            {
                first = false;
            }
            sb.append(f.getSummary());
        }
        
        return sb.toString();
    }
    
    /**
     * Get the additional information for this case
     * @return The additional information
     */
    public String getAdditional()
    {
        return additional;
    }
 
    /**
     * Get the config for this case
     * @return The config
     */
    public ImmutableNode getConfig()
    {        
        ImmutableNode Iname = new ImmutableNode.Builder().name("name").value(name).create();
        ImmutableNode Iadditional = new ImmutableNode.Builder().name("additional").value(additional).create(); 
        
        ImmutableNode.Builder config = new ImmutableNode.Builder().name("case")                
                .addChild(Iname);

        for (VCFFilter filter: filters)
        {
            config.addChild(filter.getConfig());
        }
        
        config.addChild(caller.getConfig());
        
        config.addChild(imputer.getConfig());
        
        config.addChild(combiner.getConfig());
        
        config.addChild(print.getConfig());
        
        config.addChild(Iadditional);
        
        return config.create();
    }
    
    /**
     * Get the final imputation stage config for this case
     * @param caller The caller to be used in the final imputation stage
     * @param imputer The optimized imputer to be used in the final imputation stage
     * @param combiner The optimized combiner to be used in the final imputation stage
     * @return The config
     */
    public ImmutableNode getImputeConfig(Caller caller, Imputer imputer, Combiner combiner)
    {        
        ImmutableNode Iname = new ImmutableNode.Builder().name("name").value(name).create();        
        
        ImmutableNode.Builder config = new ImmutableNode.Builder().name("case")                
                .addChild(Iname);

        for (VCFFilter filter: filters)
        {
            config.addChild(filter.getConfig());
        }
        
        config.addChild(caller.getConfig());
        
        config.addChild(imputer.getConfig());
        
        config.addChild(combiner.getConfig());
        
        return config.create();
    }
    
    private CombinerOption combiner;
    private ImputationOption imputer;
    private Caller caller;
    private PrintStats print;
    private List<VCFFilter> filters;
    private String name;
    private String additional;
}
