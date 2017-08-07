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

import Exceptions.OutputException;
import Exceptions.ProgrammerException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Represents the output optins to LinkImputeR
 * @author Daniel Money
 * @version 0.9
 */
public class Output
{

    /**
     * Constructor
     * @param summary The file to write the summary results to
     * @param table The file to write the table results to
     * @param control The file to write the control file (for the final
     * imputation stage) to
     * @param partial Whether to return partial results (i.e. for the imputation
     * and calling steps)
     */
    public Output(File summary, File table, File control, boolean partial)
    {
        this.summary = summary;
        this.table = table;
        this.control = control;
        this.partial = partial;
    }
    
    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */    
    public Output(HierarchicalConfiguration<ImmutableNode> params)
    {
        summary = new File(params.getString("summary"));
        String ts = params.getString("table",null);
        if (ts != null)
        {
            table = new File(ts);
        }
        control = new File(params.getString("control"));
        partial = params.getBoolean("partial",false);
    }
    
    /**
     * Gets the writer to write summary information to
     * @return The writer
     * @throws OutputException If there is an IO problem
     */
    public PrintWriter getSummaryWriter() throws OutputException
    {
        try
        {
            return new PrintWriter(new BufferedWriter(
                        new FileWriter(summary)));
        }
        catch (IOException ex)
        {
            throw new OutputException("Problem writing summary file");
        }
    }

    /**
     * Gets the writer to write table information to
     * @return The writer
     * @throws OutputException If there is an IO problem
     */
    public PrintWriter getTableWriter() throws OutputException
    {
        if (table != null)
        {
            try
            {
                return new PrintWriter(new BufferedWriter(
                        new FileWriter(table)));
            }
            catch (IOException ex)
            {
                throw new OutputException("Problem writing table file");
            }
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Should we write partial output
     * @return Whether to write partial output
     */
    public boolean getPartial()
    {
        //THERE SHOULD PROBABLY JUST BE A PRINT FUNCTION HERE, CALLED WITH STATS ETC  
        return partial;
    }    
   
    /**
     * Write the control file (for the final imputation step)
     * @param config The configuration to write
     * @throws OutputException If there is an IO problem
     */
    public void writeControl(List<ImmutableNode> config) throws OutputException
    {
        BasicConfigurationBuilder<ExtendedXMLConfiguration> builder = 
                new BasicConfigurationBuilder<>(ExtendedXMLConfiguration.class)
                .configure(new Parameters().xml());
        
        ExtendedXMLConfiguration c;
        try
        {
            c = builder.getConfiguration();
        }
        catch (ConfigurationException ex)
        {
            throw new ProgrammerException(ex);
        }
        
        c.setRootElementName("linkimputepro");
        
        c.addNodes(null, config);

        FileHandler handler = new FileHandler(c);
        try
        {
            handler.save(control);
        }
        catch (ConfigurationException ex)
        {
            if (ex.getCause() instanceof IOException)
            {
                IOException tex = (IOException) ex.getCause();
                throw new OutputException("Problem writing imputation control file", tex);
            }
            throw new ProgrammerException(ex);
        }        
    }
    
    /**
     * Get the input config for the final imputation step
     * @return The config
     */
    public ImmutableNode getConfig()
    {        
        ImmutableNode Isummary = new ImmutableNode.Builder().name("summary").value(summary).create();
        ImmutableNode Icontrol = new ImmutableNode.Builder().name("control").value(control).create();
        
        ImmutableNode.Builder config = new ImmutableNode.Builder().name("output")                
                .addChild(Isummary)
                .addChild(Icontrol);  
        
        if (partial)
        {
            config.addChild(new ImmutableNode.Builder().name("partial").value("true").create());
        }
        if (table != null)
        {
            config.addChild(new ImmutableNode.Builder().name("table").value(table).create()); 
        }
        
        return config.create();
    }
    
    private boolean partial;
    private File table;
    private File control;
    private File summary;
}
