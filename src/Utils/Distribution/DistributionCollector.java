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

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collector for use with streams that creates a Distribution from
 * a stream of objects
 * @author Daniel Money
 * @version 0.9
 * @param <V> The type of objects
 */
public class DistributionCollector<V> implements Collector<V,CountMap<V>,Distribution>
{
    public Set<java.util.stream.Collector.Characteristics> characteristics() 
    {
	return EnumSet.of(Characteristics.UNORDERED);
    }
    
    @Override
    public Function<CountMap<V>,Distribution> finisher()
    {
        return Distribution::new;
    }
    
    @Override
    public BinaryOperator<CountMap<V>> combiner()
    {
        return (x, y) ->
            {
                x.addAll(y);
                return x;
            };
    }
    
    @Override
    public BiConsumer<CountMap<V>,V> accumulator()
    {
        return (c, v) ->
            {
                c.add(v);
            };
    }
    
    @Override
    public Supplier<CountMap<V>> supplier()
    {
        return CountMap<V>::new;
    }
}
