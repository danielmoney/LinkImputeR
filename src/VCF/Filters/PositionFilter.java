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

package VCF.Filters;

import VCF.Exceptions.VCFNoDataException;
import VCF.Exceptions.VCFUnexpectedDataException;
import VCF.Position;
import VCF.VCF;

/**
 * Represents a position filter
 * @author Daniel Money
 * @version 0.9
 */
public abstract class PositionFilter implements VCFFilter
{

    /**
     * Tests whether a position should be filtered out
     * @param p The position to test
     * @return Whether this position should be filtered out
     */
    public abstract boolean test(Position p) throws VCFNoDataException, VCFUnexpectedDataException;
    
    public void change(VCF vcf) throws VCFNoDataException, VCFUnexpectedDataException
    {
        vcf.filterPositions(this);
    }
}
