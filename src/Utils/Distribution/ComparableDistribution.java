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

import java.util.Comparator;
import java.util.stream.Stream;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Represents a distribution of comparable discrete objects.  Each object has a
 * count associated with it
 * @author Daniel Money
 * @version 0.9
 * @param <V> The type of the comparable objects
 */
public class ComparableDistribution<V extends Comparable<V>> extends Distribution<V>
{

    /**
     * Default constructor
     * @param counts Map from object to count
     */
    public ComparableDistribution(CountMap<V> counts)
    {
        super(counts);
        min = counts.keySet().stream().reduce(this::min).orElse(null);
        max = counts.keySet().stream().reduce(this::max).orElse(null);
    }
    
    /**
     * Gets the minimum object
     * @return The minimum object
     */
    public V min()
    {
        return min;
    }
    
    /**
     * Gets the maximum object
     * @return The maximum object
     */
    public V max()
    {
        return max;
    }
    
    /**
     * Create a new distribution only containing objects between lower and upper
     * @param lower The lower object
     * @param upper The upper object
     * @return The new distribution
     */
    public ComparableDistribution<V> limitTo(V lower, V upper)
    {
        Range<V> range = Range.between(lower, upper);
        CountMap<V> newmap = new CountMap<>();
        super.allCounts().filter(p -> range.contains(p.getLeft())).forEach(p -> newmap.add(p.getLeft(),p.getRight()));
        return new ComparableDistribution<>(newmap);
    }
    
    /**
     * Get a map from object to count for all objects
     * @return Map of counts
     */
    public Stream<ImmutablePair<V,Integer>> allCounts()
    {
        return super.allCounts().sorted(Comparator.comparing(ImmutablePair::getLeft));
    }
    
    /**
     * Got a map from object to proportion (of total count) for each object
     * @return Map of proportions
     */
    public Stream<ImmutablePair<V,Double>> allProportions()
    {
        return super.allProportions().sorted(Comparator.comparing(ImmutablePair::getLeft));
    }
    
    /**
     * Returns the count of objects less than the given object
     * @param v The given object
     * @return Count of objects
     */
    public int countLessThan(V v)
    {
        return allCounts().filter(p -> (p.getLeft().compareTo(v) < 0)).mapToInt(ImmutablePair::getRight).sum();
    }
    
    /**
     * Returns the proportion of objects less than the given object
     * @param v The given object
     * @return Proportion of objects
     */
    public double proportionLessThan(V v)
    {
        return allProportions().filter(p -> (p.getLeft().compareTo(v) < 0)).mapToDouble(ImmutablePair::getRight).sum();
    }

    /**
     * Returns the count of objects less than or equal the given object
     * @param v The given object
     * @return Count of objects
     */
    public int countLessThanEqual(V v)
    {
        return allCounts().filter(p -> (p.getLeft().compareTo(v) <= 0)).mapToInt(ImmutablePair::getRight).sum();
    }
    
    /**
     * Returns the proportion of objects less than or equal to the given object
     * @param v The given object
     * @return Proportion of objects
     */
    public double proportionLessThanEqual(V v)
    {
        return allProportions().filter(p -> (p.getLeft().compareTo(v) <= 0)).mapToDouble(ImmutablePair::getRight).sum();
    }

    /**
     * Returns the count of objects greater than the given object
     * @param v The given object
     * @return Count of objects
     */
    public int countGreaterThan(V v)
    {
        return allCounts().filter(p -> (p.getLeft().compareTo(v) > 0)).mapToInt(ImmutablePair::getRight).sum();
    }

    /**
     * Returns the proportion of objects greater than the given object
     * @param v The given object
     * @return Proportion of objects
     */    
    public double proportionGreaterThan(V v)
    {
        return allProportions().filter(p -> (p.getLeft().compareTo(v) > 0)).mapToDouble(ImmutablePair::getRight).sum();
    }

    /**
     * Returns the count of objects greater than or equal to the given object
     * @param v The given object
     * @return Count of objects
     */
    public int countGreaterThanEqual(V v)
    {
        return allCounts().filter(p -> (p.getLeft().compareTo(v) >= 0)).mapToInt(ImmutablePair::getRight).sum();
    }
    
    /**
     * Returns the proportion of objects greater than or equal to the given object
     * @param v The given object
     * @return Proportion of objects
     */  
    public double proportionGreaterThanEqual(V v)
    {
        return allProportions().filter(p -> (p.getLeft().compareTo(v) >= 0)).mapToDouble(ImmutablePair::getRight).sum();
    }
    
    /**
     * Creates a constant distribution - ia distribution containing a single object
     * @param <W> Type of the object
     * @param w The object
     * @return The constant distribution
     */
    public static <W extends Comparable<W>> ComparableDistribution<W> constantDistribution(W w)
    {
        CountMap<W> c = new CountMap<>();
        c.add(w);
        return new ComparableDistribution<>(c);
    }
    
    private V min(V v1, V v2)
    {
        if (v1.compareTo(v2) < 0)
        {
            return v1;
        }
        else
        {
            return v2;
        }
    }
    
    private V max(V v1, V v2)
    {
        if (v1.compareTo(v2) > 0)
        {
            return v1;
        }
        else
        {
            return v2;
        }
    }
    
    private final V min;
    private final V max;
}
