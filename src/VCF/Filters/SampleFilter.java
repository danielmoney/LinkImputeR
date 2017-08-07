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

import VCF.Exceptions.VCFDataException;
import VCF.Sample;
import VCF.VCF;

/**
 * Represents a sample filter
 * @author Daniel Money
 * @version 0.9
 */
public abstract class SampleFilter implements VCFFilter
{

    /**
     * Tests whether a sample should be filtered out
     * @param s The sample to test
     * @return Whether this sample should be filtered out
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public abstract boolean test(Sample s) throws VCFDataException;
    
    public void change(VCF vcf) throws VCFDataException
    {
        vcf.filterSamples(this);
    }
}
