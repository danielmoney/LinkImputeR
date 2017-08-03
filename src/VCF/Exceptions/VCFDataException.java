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
public class VCFDataException extends Exception
{

    /**
     * Creates a new instance of <code>VCFDataException</code> without detail
     * message.
     */
    public VCFDataException()
    {
    }

    /**
     * Constructs an instance of <code>VCFDataException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public VCFDataException(String msg)
    {
        super(msg);
    }
    
    public VCFDataException(String msg, Throwable e)
    {
        super(msg, e);
    }
}
