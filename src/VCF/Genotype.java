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

package VCF;

import VCF.Exceptions.VCFNoDataException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a single genotype in a VCF file
 * @author Daniel Money
 * @version 1.1.3
 */
public class Genotype
{
    Genotype(RawGenotype geno, PositionMeta position, String sample)
    {
        this.geno = geno;
        this.position = position;
        this.sample = sample;
    }
    
    /**
     * Get the position metadata for the position associated with this genotype
     * @return Position metadata
     */
    public PositionMeta getPositionMeta()
    {
        return position;
    }
    
    /**
     * Get the sample name for the name associated with this genotype
     * @return The sample name
     */
    public String getSampleName()
    {
        return sample;
    }
    
    /**
     * Get all the data associated with this genotype
     * @return The data as a string
     */
    public String getData()
    {
        return geno.getInfo();
    }
    
    /**
     * Get a specified piece of data for this genotype.  Returns "." if field is
     * not present for this genotype.
     * @param name The format of the data to be retrieved (as a string)
     * @return The data
     * @throws VCF.Exceptions.VCFNoDataException If there is a no data for the
     * requested format
     */
    public String getData(String name) throws VCFNoDataException
    {
        List<String> format = position.getFormat();
        int pos = format.indexOf(name);
        
        String info = geno.getInfo();
        if (pos == -1)
        {
            throw new VCFNoDataException("No data field called " + name);
        }
        
        if (StringUtils.countMatches(info, ':') < pos)
        {
            return ".";
        }
        else
        {
            int start;
            if (pos == 0)
            {
                start = 0;
            }
            else
            {
                start = StringUtils.ordinalIndexOf(info, ":", pos) + 1;
            }
            int end = StringUtils.ordinalIndexOf(info, ":", pos + 1);
            if (end == -1)
            {
                end = info.length();
            }
            
            return info.substring(start, end);
            
//            Pattern p = Pattern.compile("(?:\\S+?:){" + pos + "}(\\S+?)(?::|$)");
//            Matcher m = p.matcher(geno.getInfo());
//            m.find();
//            return m.group(1);
        }
    }
    
    /**
     * Change a particular piece of data associated with the genotype
     * @param name The format of the data to be changed (as a string)
     * @param value The new value
     * @throws VCF.Exceptions.VCFNoDataException If there is a no data for the
     * requested format
     */
    public void replaceData(String name, String value) throws VCFNoDataException
    {
        List<String> format = position.getFormat();
        int pos = format.indexOf(name);
        if (pos == -1)
        {
            throw new VCFNoDataException("No data field called " + name);
        }
        
        String info = geno.getInfo();
        int start = 0;
        if (pos > 0)
        {
            start = StringUtils.ordinalIndexOf(info, ":", pos) + 1;
        }
        int end = info.length();
        if (pos < format.size() - 1)
        {
            end = StringUtils.ordinalIndexOf(info, ":", pos + 1);
            if (end == -1)
            {
                throw new VCFNoDataException("Data field " + name + " is not present for this genotype");
            }
        }
        geno.setInfo(StringUtils.overlay(info, value, start, end));
    }
    
    /**
     * Add data to the genotype.  It is up to the caller to ensure the appropriate
     * format is also added to the position.
     * @param value The value to add
     */
    public void addData(String value)
    {
        geno.setInfo(geno.getInfo() + ":" + value);
    }
    
    /**
     * Remove data for the given format.  It is up to the caller to ensure the
     * appropriate format is removed from the position.
     * @param name The name of the format data to remove
     * @throws VCFNoDataException If there is no data field with that name
     */
    public void removeData(String name) throws VCFNoDataException
    {
        List<String> format = position.getFormat();
        int pos = format.indexOf(name);
        if (pos == -1)
        {
            throw new VCFNoDataException("No data field called " + name);
        }

        String info = geno.getInfo();
        int start = 0;
        if (pos > 0)
        {
            start = StringUtils.ordinalIndexOf(info, ":", pos);
        }
        int end = info.length();
        if (pos < format.size() - 1)
        {
            end = StringUtils.ordinalIndexOf(info, ":", pos + 1);
        }
        geno.setInfo(StringUtils.overlay(info, "", start, end));
    }
    
    @Override
    public String toString()
    {
        return position.toString() + "\t" + sample + "\t" + geno.getInfo();
    }
    
    /**
     * Create a copy of the genotype
     * @return A copy of the genotype
     */
    public Genotype copy()
    {
        return new Genotype(geno.copy(), position, sample);
    }
    
    private final PositionMeta position;
    private final String sample;
    private final RawGenotype geno;
}
