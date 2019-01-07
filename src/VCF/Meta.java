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
import java.util.stream.Stream;

/**
 * Represents the meta information in a VCF
 */
public class Meta
{

    /**
     * Constructor from the meta lines in a VCF
     * @param lines The meta lines
     */
    public Meta(List<String> lines)
    {
        this.lines = lines;
        this.formats = new ArrayList<>();
        for (String line: lines)
        {
            if (line.startsWith("##FORMAT=<ID="))
            {
                int commapos = line.indexOf(',');
                formats.add(line.substring(13,commapos));
            }
        }
    }
    
    /**
     * Tests whether the input format is included in the meta information
     * @param format The format
     * @return Whether the format is included in the meta information
     */
    public boolean hasFormat(String format)
    {
        return formats.contains(format);
    }
    
    /**
     * Removes the given format from the meta information
     * @param format The format to remove
     */
    public void removeFormat(String format)
    {
        formats.remove(format);
        String matched = null;
        for (String line: lines)
        {
            if (line.startsWith("##FORMAT=<ID=" + format))
            {
                matched = line;
            }
        }
        if (matched != null)
        {
            lines.remove(matched);
        }
    }
    
    /**
     * Adds a format to the meta information
     * @param format The format's code
     * @param line The full line to add
     */
    public void addFormat(String format, String line)
    {
        formats.add(format);
        lines.add(line);
    }
    
    /**
     * Get the meta lines
     * @return A list of meta lines
     */
    public List<String> getLinesList()
    {
        return lines;
    }
    
    /**
     * Get the meta lines
     * @return A stream of meta lines
     */
    public Stream<String> getLinesStream()
    {
        return lines.stream();
    }
    
    private final List<String> lines;
    private final List<String> formats;
}
