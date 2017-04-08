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

package Utils.SingleGenotype;

/**
 * Represents a single genotype call
 * @author Daniel Money
 * @version 0.9
 */
public class SingleGenotypeCall extends SingleGenotypePosition
{

    /**
     * Constructor
     * @param sample Sample position
     * @param snp Snp position
     * @param call The call
     */
    public SingleGenotypeCall(int sample, int snp, byte call)
    {
        super(sample,snp);
        this.call = call;
    }
    
    /**
     * Get the call
     * @return The call
     */
    public byte getCall()
    {
        return call;
    }
    
    private byte call;
}
