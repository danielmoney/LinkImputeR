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

package VCF.Changers;

import VCF.Exceptions.VCFDataException;
import VCF.Position;

/**
 * Changes a position
 */
public interface PositionChanger
{

    /**
     * Change the given genotype
     * @param p The position to change
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public void change(Position p) throws VCFDataException;
}
