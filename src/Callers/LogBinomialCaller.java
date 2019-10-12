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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Simple binomial caller
 * @author Daniel Money
 * @version 1.1.3
 */
public class LogBinomialCaller extends LogBiasedBinomialCaller
{

    /**
     * Creates the caller with the given error
     * @param error The error rate
     */
    public LogBinomialCaller(double error)
    {
        super(error,0.5);
    }

    /**
     * Creates the caller from the given configuration
     * @param params The configuration
     */
    public LogBinomialCaller(HierarchicalConfiguration<ImmutableNode> params)
    {
        super(params.getDouble("error"),0.5);
    }
    
    public ImmutableNode getConfig()
    {
        ImmutableNode Ierror = new ImmutableNode.Builder().name("error").value(error).create();       
        
        ImmutableNode config = new ImmutableNode.Builder().name("caller")
                .addChild(Ierror)
                .addAttribute("name", "LogBinomial")
                .create();
        
        return config;
    }
}
