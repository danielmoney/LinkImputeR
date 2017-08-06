/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VCF.Exceptions;

/**
 *
 * @author daniel
 */
public class VCFException extends Exception
{

    /**
     * Creates a new instance of <code>VCFException</code> without detail
     * message.
     */
    public VCFException()
    {
    }

    /**
     * Constructs an instance of <code>VCFException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public VCFException(String msg)
    {
        super(msg);
    }
    
    public VCFException(String msg, Throwable cause)
    {
        super(msg,cause);
    }
}
