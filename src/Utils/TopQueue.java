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

import java.util.Arrays;

/**
 * Queue that keeps only the entries with the top values
 * @author Daniel Money
 * @version 0.9
 */
public class TopQueue
{

    /**
     * Constructor
     * @param top The number of top entries to store
     */
    public TopQueue(int top)
    {
        this.entries = new int[top];
        this.values = new double[top];
        Arrays.fill(values, -Double.MAX_VALUE);
        this.min = -Double.MAX_VALUE;
        this.top = top;
    }
    
    /**
     * Attempts to add a new entry
     * @param e The entry
     * @param v The entry's value
     * @return Whether the entry was added.  Returns false if the entry
     * was smaller than the smallest element already in the queue and the
     * queue is full.
     */
    public synchronized boolean add(int e, double v)
    {
        if (v > min)
        {
            int p = 0;
            while (values[p] > v)
            {
                p++;
            }
            
            int[] newEntries = new int[top];
            System.arraycopy(entries, 0, newEntries, 0, p);
            newEntries[p] = e;
            System.arraycopy(entries, p, newEntries, p+1, top-p-1);
            
            double[] newValues = new double[top];
            System.arraycopy(values, 0, newValues, 0, p);
            newValues[p] = v;
            System.arraycopy(values, p, newValues, p+1, top-p-1);
            
            values = newValues;
            entries = newEntries;
            
            min = values[top - 1];
            
            return true;
        }
        else
        {
            return false;
        }
    }
   
    /**
     * Returns the entries in the queue as an array
     * @return Ordered list of entries
     */
    public int[] getList()
    {
        return entries;
    }
    
    /**
     * Returns the entries in the queue as an array.  Only the top n 
     * entries are returned, or all the entries in the list if n is greater
     * than the size of the queue
     * @param n The number of entries to return
     * @return Ordered list of entries
     */
    public int[] getList(int n)
    {
        return Arrays.copyOf(entries, n);
    }
    
    /**
     * Returns the entries in the values as an array
     * @return Ordered list of entries
     */
    public double[] getValueList()
    {
        return values;
    }
 
    /**
     * Returns the values in the queue as an array.  Only the top n 
     * entries are returned, or all the entries in the list if n is greater
     * than the size of the queue
     * @param n The number of entries to return
     * @return Ordered list of entries
     */   
    public double[] getValueList(int n)
    {
        return Arrays.copyOf(values, n);
    }
   
    private final int top;
    private double min;
    private int[] entries;
    private double[] values;
}
