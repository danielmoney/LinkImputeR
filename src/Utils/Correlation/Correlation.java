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

package Utils.Correlation;

import Utils.Progress.Progress;
import Utils.Progress.ProgressFactory;
import Utils.TopQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Calculates correlations.  Mainly has utility functions that use functions of
 * implementing classes to calculate multiple correlations.
 * @author daniel
 */
public abstract class Correlation
{
    
    /**
     * Calculates correlations between all pairs of arrays
     * @param data The data
     * @return A symmetric array of correlations
     */
    public double[][] calculate(byte[][] data)
    {
        Progress progress = ProgressFactory.get(data.length * (data.length - 1) / 2);
        double[][] result = new double[data.length][data.length];
        IntStream.range(0, data.length).forEach(i ->
            {
            IntStream.range(i+1,data.length).parallel().forEach(j ->
                {
                    double c = calculate(data[i],data[j]);
                    result[i][j] = c;
                    result[j][i] = c;
                });
            progress.done(data.length - i - 1);        
            });
        return result;
    }
    
    /**
     * Returns the position of the most correlated arrays for every array.  The
     * n most correlated arrays are returned
     * @param data The data
     * @param n Return this number of top correlated arrays
     * @return Map from the position of an array to the position of the n arrays
     * most correlated with it
     */
    public Map<Integer, int[]> topn(byte[][] data, int n)
    {
        Progress progress = ProgressFactory.get(data.length * (data.length - 1) / 2);
        
;
        Map<Integer,TopQueue> tq = new HashMap<>();
        
        for (int i = 0; i < data.length; i++)
        {
            tq.put(i,new TopQueue(n));
        }
        
        IntStream.range(0, data.length).forEach(i ->
            {
            IntStream.range(i+1,data.length).parallel().forEach(j ->
                {
                    double c = calculate(data[i],data[j]);
                    tq.get(i).add(j, c);
                    tq.get(j).add(i, c);
                });
            progress.done(data.length - i - 1); 
            });
        
        Map<Integer,int[]> result = new HashMap<>();
        for (Entry<Integer,TopQueue> e: tq.entrySet())
        {
            result.put(e.getKey(),e.getValue().getList());
        }
        return result;
    }
    
    /**
     * Similar to topn except it returns the correlations rather than the
     * positions
     * @param data The data
     * @param n Return this number of top correlated arrays
     * @return Map from the position of an array to the correlation of the n arrays
     * most correlated with it
     */
    public Map<Integer, double[]> topnvalues(byte[][] data, int n)
    {
        Progress progress = ProgressFactory.get(data.length * (data.length - 1) / 2);
        
        //Map<Integer,TopQueue<Integer,Double>> tq = new HashMap<>();
        Map<Integer,TopQueue> tq = new HashMap<>();
        
        for (int i = 0; i < data.length; i++)
        {
            //tq.put(i,new TopQueue<>(n,true));
            tq.put(i,new TopQueue(n));
        }
        
        IntStream.range(0, data.length).forEach(i ->
            {
            IntStream.range(i+1,data.length).parallel().forEach(j ->
                {
                    double c = calculate(data[i],data[j]);
                    tq.get(i).add(j, c);
                    tq.get(j).add(i, c);
                });
            progress.done(data.length - i - 1); 
            });
        
        Map<Integer,double[]> result = new HashMap<>();
        for (Entry<Integer,TopQueue> e: tq.entrySet())
        {
            result.put(e.getKey(),e.getValue().getValueList());
        }
        return result;
    }
    
    /**
     * Similar to topn except it only considers the given arrays
     * @param data The data
     * @param n Return this number of top correlated arrays
     * @param list The position of the arrays to consider
     * @return Map from the position of an array to the position of the n arrays
     * in the list most correlated with it
     */
    public Map<Integer, int[]> limitedtopn(byte[][] data, int n, Set<Integer> list)
    {
        Progress progress = ProgressFactory.get(list.size());
        
        Map<Integer,TopQueue> tq = new HashMap<>();
        
        for (Integer i: list)
        {
            tq.put(i,new TopQueue(n));
        }
        
        list.stream().forEach(i ->
            {
            IntStream.range(0,data.length).parallel().filter(j -> (i != j)).forEach(j ->
                {
                    double c = calculate(data[i],data[j]);
                    tq.get(i).add(j, c);
                });
            progress.done(); 
            });
        
        Map<Integer,int[]> result = new HashMap<>();
        for (Entry<Integer,TopQueue> e: tq.entrySet())
        {
            result.put(e.getKey(),e.getValue().getList());
        }
        return result;
    }
    
    /**
     * Similar to limitedtopn except it returns correlations
     * @param data The data
     * @param n Return this number of top correlated arrays
     * @param list The position of the arrays to consider
     * @return Map from the position of an array to the correlation of the n arrays
     * in the list most correlated with it
     */
    public Map<Integer, double[]> limitedtopnvalues(byte[][] data, int n, Set<Integer> list)
    {
        Progress progress = ProgressFactory.get(list.size());
        
        //Map<Integer,TopQueue<Integer,Double>> tq = new HashMap<>();
        Map<Integer,TopQueue> tq = new HashMap<>();
        
        for (Integer i: list)
        {
            //tq.put(i,new TopQueue<>(n,true));
            tq.put(i,new TopQueue(n));
        }
        
        list.stream().forEach(i ->
            {
            IntStream.range(0,data.length).parallel().filter(j -> (i != j)).forEach(j ->
                {
                    double c = calculate(data[i],data[j]);
                    tq.get(i).add(j, c);
                });
            progress.done(); 
            });
        
        //Map<Integer,List<Integer>> result = new HashMap<>();
        Map<Integer,double[]> result = new HashMap<>();
        //for (Entry<Integer,TopQueue<Integer,Double>> e: tq.entrySet())
        for (Entry<Integer,TopQueue> e: tq.entrySet())
        {
            result.put(e.getKey(),e.getValue().getValueList());
        }
        return result;
    }
    
    /**
     * Calculates the correlation between two arrays
     * @param d1 The first array
     * @param d2 The second array
     * @return The correlation
     */
    public abstract double calculate(byte[] d1, byte[] d2);
}
