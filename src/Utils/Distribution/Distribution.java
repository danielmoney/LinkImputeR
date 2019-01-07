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

package Utils.Distribution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Represents a discrete distribution of objects.  Each object has a count
 * associated with it
 * @author Daniel Money
 * @version 1.1.3
 * @param <V> The type of object
 */
public class Distribution<V>
{    

    /**
     * Default constructor
     * @param counts Map from object to count
     */
    public Distribution(CountMap<V> counts)
    {
        this.counts = counts;
        total = counts.values().stream().reduce(0, Integer::sum);
                        
        proportion = new HashMap<>();
        for (Entry<V,Integer> e: counts.entrySet())
        {
            proportion.put(e.getKey(), (double) e.getValue() / (double) total);
        }
    }
    
    /**
     * Get the proportion for n object.  That is count / total count
     * @param v The object
     * @return The proportion
     */
    public double getProportion(V v)
    {
        return proportion.get(v);
    }
    
    /**
     * Get the count for an object
     * @param v The object
     * @return The count
     */
    public int getCount(V v)
    {
        return counts.get(v);
    }
    
    /**
     * Get the total count for all objects
     * @return Total count
     */
    public int getTotal()
    {
        return total;
    }
    
    /**
     * Gets a stream of (object, count) pairs
     * @return The stream
     */
    public Stream<ImmutablePair<V,Integer>> allCounts()
    {
        return counts.entrySet().stream().map(e -> new ImmutablePair<>(e.getKey(),e.getValue()));
    }
    
    /**
     * Get a stream of (object, proportion) pairs
     * @return The stream
     */
    public Stream<ImmutablePair<V,Double>> allProportions()
    {
        return proportion.entrySet().stream().map(e -> new ImmutablePair<>(e.getKey(),e.getValue()));
    }
    
    /**
     * Get a sample from the distribution
     * @return The sampled object
     */
    public V sample()
    {
        double s = Math.random();
        V ret = null;
        double t = 0.0;
        for (Entry<V,Double> e: proportion.entrySet())
        {
            if (t < s)
            {
                ret = e.getKey();
            }
            t += e.getValue();
        }
        return ret;
    }
    
    /**
     * Creates a constant distribution - ia distribution containing a single object
     * @param <W> Type of the object
     * @param w The object
     * @return The constant distribution
     */
    public static <W> Distribution<W> constantDistribution(W w)
    {
        CountMap<W> c = new CountMap<>();
        c.add(w);
        return new Distribution<>(c);
    }
    
    final int total;
    final Map<V,Double> proportion;
    final CountMap<V> counts;
    List<ImmutablePair<V,Integer>> list;
}
