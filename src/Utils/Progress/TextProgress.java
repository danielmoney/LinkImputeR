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

package Utils.Progress;

import org.apache.commons.lang3.StringUtils;

/**
 * Shows progress as a simple tex output showing" number complete / total number"
 * @author Daniel Money
 * @version 1.1.3
 */
public class TextProgress implements Progress
{

    /**
     * Constructor
     * @param total The number of tasks to be done
     */
    public TextProgress(long total)
    {
        this.total = Long.toString(total);
        this.tl = this.total.length();
    }
    
    public void done()
    {
        done(1);
    }
    
    public void done(int number)
    {
        done += number;
        System.out.println("\t" + StringUtils.leftPad(Long.toString(done), tl) + "/" + total);
    }
    
    private long done;
    private final int tl;
    private final String total;
}
