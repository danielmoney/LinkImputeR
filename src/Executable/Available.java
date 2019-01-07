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

import Callers.BiasedBinomialCaller;
import Callers.BinomialCaller;
import Callers.Caller;
import Exceptions.ProgrammerException;
import VCF.Filters.BiallelicFilter;
import VCF.Filters.MAFFilter;
import VCF.Filters.ParalogHWFilter;
import VCF.Filters.PositionFilter;
import VCF.Filters.PositionMissing;
import VCF.Filters.SampleFilter;
import VCF.Filters.SampleMissing;
import VCF.Filters.VCFFilter;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

class Available
{
    private Available()
    {
        
    }
    
    static VCFFilter getFilter(HierarchicalConfiguration<ImmutableNode> config)
    {
        if (pf.has(config.getString("[@name]")))
        {
            return pf.get(config.getString("[@name]"), config);
        }
        if (sf.has(config.getString("[@name]")))
        {
            return sf.get(config.getString("[@name]"), config);
        }
        
        //SHOULD PROBABLY THROW EXCEPTION!
        throw new ProgrammerException();
    }
    
    static PositionFilter getPositionFilter(HierarchicalConfiguration<ImmutableNode> config)
    {
        if (pf.has(config.getString("[@name]")))
        {
            return pf.get(config.getString("[@name]"), config);
        }
        
        //SHOULD PROBABLY THROW EXCEPTION!
        throw new ProgrammerException();
    }
    
    static Caller getCaller(HierarchicalConfiguration<ImmutableNode> config)
    {
        if (callers.has(config.getString("[@name]")))
        {
            return callers.get(config.getString("[@name]"), config);
        }
        
        //SHOULD PROBABLY THROW EXCEPTION!
        throw new ProgrammerException();
    }
    
    private static final ClassList<HierarchicalConfiguration<ImmutableNode>,Caller> callers = new ClassList<>();
    private static final ClassList<HierarchicalConfiguration<ImmutableNode>,SampleFilter> sf = new ClassList<>();
    private static final ClassList<HierarchicalConfiguration<ImmutableNode>,PositionFilter> pf = new ClassList<>();
    
    static
    {
        // Position Filters
        pf.add("MAF",MAFFilter::new);
        pf.add("ParalogHW",ParalogHWFilter::new);
        pf.add("PositionMissing",PositionMissing::new);
        pf.add("BiallelicGap",BiallelicFilter::new);
        
        // Sample Filters
        sf.add("SampleMissing",SampleMissing::new);
        
        // Callers
        callers.add("Binomial",BinomialCaller::new);
        callers.add("BiasedBinomial",BiasedBinomialCaller::new);
    }
}
