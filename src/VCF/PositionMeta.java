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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the meta data for a position
 * @author Daniel Money
 * @version 0.9
 */
public class PositionMeta
{

    /**
     * Constructor from a string representing the meta data (tab delimited)
     * @param meta String representing the meta data
     */
    public PositionMeta(String meta)
    {
        this(meta.split("\\t"));
    }
    
    /**
     * Constructor from an array of strings representing the meta data
     * @param parts Array of strings representing the meta data
     */
    public PositionMeta(String[] parts)
    {
        chrom = parts[0];
        position = parts[1];
        id = parts[2];
        ref = parts[3];
        alt = parts[4].split(",");
        qual = parts[5];
        filter = parts[6];
        
        //Should probably do something more sensible with this
        info = parts[7];
        
        String[] formats = parts[8].split(":");
        format = new ArrayList<>();
        for (String f: formats)
        {
            format.add(f);
        }
    }
    
    /**
     * Gets a string representing just the chromosome and the position of the
     * position.
     * @return The string
     */
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append(chrom);
        s.append(":");
        s.append(position);
        return s.toString();
    }
    
    /**
     * Get a string for all the meta data for the position
     * @return The string
     */
    public String toText()
    {
        StringBuilder s = new StringBuilder();
        s.append(chrom); s.append("\t");
        s.append(position); s.append("\t");
        s.append(id); s.append("\t");
        s.append(ref); s.append("\t");
        for (String a: alt)
        {
            s.append(a); s.append(",");
        }
        s.deleteCharAt(s.length() - 1);
        s.append("\t");
        s.append(qual); s.append("\t");
        s.append(filter); s.append("\t");
        s.append(info); s.append("\t");
        
        for (String f: format)
        {
            s.append(f);
            s.append(":");
        }
        s.deleteCharAt(s.length() - 1);
        
        return s.toString();
    }
    
    /**
     * Get the chromosome
     * @return The chromosome
     */
    public String getChrom()
    {
        return chrom;
    }
    
    /**
     * Get the position (within a chromosome) of the position
     * @return The position
     */
    public String getPosition()
    {
        return position;
    }
    
    /**
     * Get the ID
     * @return The ID
     */
    public String getID()
    {
        return id;
    }
    
    /**
     * Get the reference allele
     * @return The reference allele
     */
    public String getRef()
    {
        return ref;
    }
    
    /**
     * Get the alternative alleles
     * @return Array of the alternative alleles
     */
    public String[] getAlt()
    {
        return alt;
    }
    
    /**
     * Get the quality
     * @return The quality
     */
    public String getQual()
    {
        return qual;
    }
    
    /**
     * Get the filters
     * @return The filters
     */
    public String getFilter()
    {
        return filter;
    }
    
    /**
     * Get the info
     * @return The info
     */
    public String getInfo()
    {
        return info;
    }
    
    /**
     * Get a list of formats
     * @return List of formats
     */
    public List<String> getFormat()
    {
        return format;
    }
    
    /**
     * Are two positions equal - based only on chromosome and position
     * @param o The position to compare to
     * @return Whether the two positions are equal
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof PositionMeta))
        {
            return false;
        }
        
        PositionMeta pm = (PositionMeta) o;
        
        return (pm.position.equals(position) && pm.chrom.equals(chrom));
    }
    
    @Override
    public int hashCode()
    {
        return position.hashCode();
    }
    
    private String chrom;
    private String position;
    private String id;
    private String ref;
    private String[] alt;
    private String qual;
    private String filter;
    private String info;
    private ArrayList<String> format;
}
