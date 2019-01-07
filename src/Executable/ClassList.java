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

package Executable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents a map of constructors so that a new object of the appropriate child
 * class (given by a string) can be constructed.  All constructors must have the
 * same type of single input.  Not the best description - probably best to see
 * how it's used!
 * @author Daniel Money
 * @version 0.9
 * @param <X> The type of the input to the constructor
 * @param <Y> The super class of the classes to be constructed
 */
public class ClassList<X,Y>
{

    /**
     * Default constructor
     */
    public ClassList()
    {
        map = new HashMap<>();
    }

    /**
     * Add a new child class
     * @param type The string representing the child class
     * @param constructor The constructor for that class
     */
    public void add(String type, Function<X,Y> constructor)
    {
        map.put(type, constructor);
    }

    /**
     * Get a new instance of the child class
     * @param type The string representing the child class
     * @param parameters The input parameter to the constructor
     * @return The new child class object
     */
    public Y get(String type, X parameters)
    {
        return map.get(type).apply(parameters);
    }
    
    /**
     * Whether the class list has a class associated with a string
     * @param type The string
     * @return Whether there is a class associated with it
     */
    public boolean has(String type)
    {
        return map.containsKey(type);
    }

    final Map<String,Function<X,Y>> map;
}

