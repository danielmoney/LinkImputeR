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

package Exceptions;

/**
 * Exception for when there is a problem with a ini file
 */
public class INIException extends Exception
{

    /**
     * Default constructor
     */
    public INIException()
    {
    }

    /**
     * Constructor that takes a message
     * @param msg The message
     */
    public INIException(String msg)
    {
        super(msg);
    }
    
    /**
     * @param msg The message
     * @param cause The cause
     */
    public INIException(String msg, Throwable cause)
    {
        super(msg,cause);
    }
}
