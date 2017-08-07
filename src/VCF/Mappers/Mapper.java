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

package VCF.Mappers;

import VCF.Exceptions.VCFUnexpectedDataException;

/**
 * Maps from string to object
 * @author Daniel Money
 * @version 0.9
 * @param <M> Type that is mapped to
 */
public interface Mapper<M>
{

    /**
     * Map from string to object
     * @param v The String
     * @return The object
     * @throws VCF.Exceptions.VCFUnexpectedDataException If there is unexpected
     * data in the VCF
     */
    public M map(String v) throws VCFUnexpectedDataException;

    /**
     * Get an array of the returned type
     * @param size Size of the array
     * @return The array
     */
    public M[] getArray(int size);

    /**
     * Get a 2d array of the returned type
     * @param size Size of the major dimension
     * @return The array
     */
    public M[][] get2DArray(int size);
}
