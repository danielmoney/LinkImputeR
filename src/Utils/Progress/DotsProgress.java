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

/*
 * This file is part of LinkImpute.
 * 
 * LinkImpute is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LinkImpute is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LinkImpute.  If not, see <http://www.gnu.org/licenses/>.
 */

package Utils.Progress;

/**
 * Draws and updates a progress bar to standard out
 * @author Daniel Money
 * @version 0.9
 */
public class DotsProgress implements Progress
{

    /**
     * Constructor
     * @param total The number of tasks to be done
     */
    public DotsProgress(long total)
    {
        shown = 0;
        done = 0;
        this.total = total;
        System.out.print("\t0");
        for (int i = 0; i < size - 2; i++)
        {
            System.out.print(" ");
        }
        System.out.println("1");
        System.out.print("\t");
    }
    
    /**
     * Call when another task has been completed
     */
    public synchronized void done()
    {
        done(1);
    }
    
    /**
     * Call when more tasks have been completed
     * @param number The number of tasks completed
     */
    public synchronized void done(int number)
    {
        done += number;
        int show = (int) Math.floor((double) size * (double) done / (double) total);
        if (show != shown)
        {
            System.out.print(".");
            shown = show;
            if (done == total)
            {
                System.out.println();
            }
        }
    }
    
    private int shown;
    private long done;
    private final long total;
    private final static int size = 50;
}
