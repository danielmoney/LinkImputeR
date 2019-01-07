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
 * Factory for creating a progress indicator
 * @author Daniel Money
 * @version 1.1.3
 */
public class ProgressFactory
{
    private ProgressFactory()
    {
        
    }
    
    /**
     * Get a new instance
     * @param total The number of tasks to be completed
     * @return A new instance
     */
    public static Progress get(long total)
    {
        switch (type)
        {
            case TEXT:
                return new TextProgress(total);
            case TEXTBAR:
                return new TextBarProgress(total);
            case DOTS:
                return new DotsProgress(total);
            case SILENT:
            default:
                return new SilentProgress();
        }
    }
    
    /**
     * Set the type of progress indicator the factory creates
     * @param type Type of progress indicator
     */
    public static void set(ProgressType type)
    {
        ProgressFactory.type = type;
    }
    
    /**
     * Represents the types of progress indicators
     */
    public enum ProgressType
    {

        /**
         * The silent indicator (SilentProgress)
         */
        SILENT,

        /**
         * The text bar indicator (TextBarProgress)
         */
        TEXTBAR,

        /**
         * The dots indicator (DotsProgress)
         */
        DOTS,

        /**
         * The text indicator (TextProgress)
         */
        TEXT
    }
    
    private static ProgressType type = ProgressType.SILENT;
}
