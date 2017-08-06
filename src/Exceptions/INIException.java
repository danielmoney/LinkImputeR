/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exceptions;

/**
 *
 * @author daniel
 */
public class INIException extends Exception
{

    /**
     * Creates a new instance of <code>INIException</code> without detail
     * message.
     */
    public INIException()
    {
    }

    /**
     * Constructs an instance of <code>INIException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public INIException(String msg)
    {
        super(msg);
    }
}
