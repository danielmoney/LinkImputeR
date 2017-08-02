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
public class VCFUnexpectedDataException extends Exception
{

    /**
     * Creates a new instance of <code>VCFUnexpectedDataException</code> without
     * detail message.
     */
    public VCFUnexpectedDataException()
    {
    }

    /**
     * Constructs an instance of <code>VCFUnexpectedDataException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public VCFUnexpectedDataException(String msg)
    {
        super(msg);
    }
    
    public VCFUnexpectedDataException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
