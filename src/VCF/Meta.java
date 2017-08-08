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

public class Meta
{
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
    
    public boolean hasFormat(String format)
    {
        return formats.contains(format);
    }
    
    public List<String> getLinesList()
    {
        return lines;
    }
    
    public Stream<String> getLinesStream()
    {
        return lines.stream();
    }
    
    private List<String> lines;
    private List<String> formats;
}
