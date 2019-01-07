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

package Utils;

import Exceptions.OutputException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Very simple logger.  Could not find a simple enough existing one!
 * @author Daniel Money
 */
public class Log
{
    private Log()
    {
    }
    
    /**
     * Log a new message
     * @param level The level of the message
     * @param message The message
     */
    public static void log(Level level, String message)
    {
        if ((pw != null) && (loglevel.compareTo(level) >= 0))
        {
            if (loglevel == Level.DEBUG)
            {
                pw.print(sdf.format(new Date()));
            }
            pw.println(message);
            pw.flush();
        }
    }
    
    /**
     * Log a new critical message
     * @param message The message
     */
    public static void critical(String message)
    {
        log(Level.CRITICAL,message);
    }
    
    /**
     * Log a new brief message
     * @param message The message
     */
    public static void brief(String message)
    {
        log(Level.BRIEF,message);
    }
    
    /**
     * Log a new detail message
     * @param message The message
     */
    public static void detail(String message)
    {
        log(Level.DETAIL,message);
    }
    
    /**
     * Log a new debug message
     * @param message The message
     */
    public static void debug(String message)
    {
        log(Level.DEBUG,message);
    }
    
    /**
     * Initialise from a configuration
     * @param params Configuration
     * @throws OutputException If there's a problem setting up the logger
     */
    public static void initialise(HierarchicalConfiguration<ImmutableNode> params) throws OutputException
    {
        String ls = params.getString("file",null);

        if (pw != null)
        {
            pw.close();
        }
        else
        {
            Runtime.getRuntime().addShutdownHook(new Closer());   
        }            
        
        if (ls != null)
        {
            try
            {
                pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(ls))));
            }
            catch (IOException ex)
            {
                throw new OutputException("Problem writing log file");
            }
        }
        else
        {
            pw = new PrintWriter(new OutputStreamWriter(System.err));
        }

        
        switch(params.getString("level","").toLowerCase())
        {
            case "brief":
                loglevel = Level.BRIEF;
                break;
            case "detail":
                loglevel = Level.DETAIL;
                break;
            case "debug":
                loglevel = Level.DEBUG;
                break;
            case "critical":
                loglevel = Level.CRITICAL;
                break;
        }
    }
    
    /**
     * Initialise a logger (logs to screen)
     * @param level Level to log
     */
    public static void initialise(Level level)
    {
        if (pw != null)
        {
            pw.close();
        }
        else
        {
            Runtime.getRuntime().addShutdownHook(new Closer());   
        }
        pw = new PrintWriter(new OutputStreamWriter(System.err));
        loglevel = level;        
    }
    
    /**
     * Initialise a logger (logs to file)
     * @param level Level to log
     * @param f File to log to
     * @throws IOException If there is a problem setting up the logger
     */
    public static void initialise(Level level, File f) throws IOException
    {
        if (pw != null)
        {
            pw.close();
        }
        else
        {
            Runtime.getRuntime().addShutdownHook(new Closer());   
        }
        pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        loglevel = level;
    }

    /**
     * Get the config for a caller
     * @param level The level to log
     * @param log The file to log to.  Null logs nothing.
     * @return The config
     */
    public static ImmutableNode getConfig(Level level, File log)
    {
        ImmutableNode.Builder config = new ImmutableNode.Builder().name("log");  
        if (log != null)
        {
            config.addChild(new ImmutableNode.Builder().name("file").value(log).create()); 
        }
        
        String ls;
        switch (level)
        {
            case DEBUG:
                ls = "debug";
                break;
            case DETAIL:
                ls = "detail";
                break;
            case BRIEF:
                ls = "brief";
                break;
            default:
                ls = "critical";
                break;
        }
        
        config.addChild(new ImmutableNode.Builder().name("level").value(ls).create());
        
        return config.create();
    }
    
    private static PrintWriter pw = null;
    private static Level loglevel;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss\t");
    
    /**
     * Represents different levels of message output.  All levels also include
     * lower levels.
     */
    public enum Level
    {

        /**
         * Only outputs critical messages
         */
        CRITICAL,

        /**
         * Only output brief messages
         */
        BRIEF,

        /**
         * Output detailed messages
         */
        DETAIL,

        /**
         * Output debug level messages
         */
        DEBUG        
    }
    
    private static class Closer extends Thread
    {
        private Closer()
        {
        }
        
        public void run()
        {
            pw.close();
        }        
    }
}
