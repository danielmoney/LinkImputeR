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

package Utils.Progress;

/**
 * A means for indicating progress to the user
 * @author Daniel Money
 * @version 1.1.3
 */
public interface Progress
{
    
    /**
     * Called to indicate another task is complete
     */
    void done();

    /**
     * Called to indicate multiple tasks are complete
     * @param number The number completed
     */
    void done(int number);
}
