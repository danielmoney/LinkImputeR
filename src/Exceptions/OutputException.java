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
public class OutputException extends Exception
{

    /**
     * Creates a new instance of <code>OutputException</code> without detail
     * message.
     */
    public OutputException()
    {
    }

    /**
     * Constructs an instance of <code>OutputException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public OutputException(String msg)
    {
        super(msg);
    }
    
    public OutputException(String msg, Throwable cause)
    {
        super(msg,cause);
    }
}
