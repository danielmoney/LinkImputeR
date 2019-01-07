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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Keeps counts of an object
 * @author Daniel Money
 * @version 1.1.3
 * @param <V> the type of the objects
 */
public class CountMap<V>
{

    /**
     * Default constructor
     */
    public CountMap()
    {
        counts = new HashMap<>();
    }
    
    /**
     * Adds a single instance of the give object.  I.e. increased the count
     * of that object by one.
     * @param v The object
     */
    public void add(V v)
    {
        add(v,1);
    }
    
    /**
     * Adds multiple instances of the give object.  I.e. increased the count
     * of that object by c.
     * @param v The object
     * @param c The number of instances to add
     */
    public void add(V v, Integer c)
    {
        if (!counts.containsKey(v))
        {
            counts.put(v, c);
        }
        else
        {
            counts.put(v, counts.get(v) + c);
        }
    }
    
    /**
     * Add the members of another count map
     * @param other The other count map
     */
    public void addAll(CountMap<V> other)
    {
        for (Entry<V,Integer> e: other.counts.entrySet())
        {
            add(e.getKey(), e.getValue());
        }
    }
    
    /**
     * Get the count of the given object
     * @param v The object
     * @return The count of that object
     */
    public Integer get(V v)
    {
        return counts.get(v);
    }
    
    /**
     * Get the set of keys (that is objects)
     * @return The set of keys
     */
    public Set<V> keySet()
    {
        return counts.keySet();
    }
    
    /**
     * Get the set of values (that is counts)
     * @return The set of values
     */
    public Collection<Integer> values()
    {
        return counts.values();
    }
    
    /**
     * Get the entry set for the map
     * @return The entry set
     */
    public Set<Entry<V,Integer>> entrySet()
    {
        return counts.entrySet();
    }
    
    private final HashMap<V,Integer> counts;
}
