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
public class VCFDataLineException extends VCFInputException
{

    /**
     * Creates a new instance of <code>VCFDataLineException</code> without
     * detail message.
     */
    public VCFDataLineException()
    {
    }

    /**
     * Constructs an instance of <code>VCFDataLineException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public VCFDataLineException(String msg)
    {
        super(msg);
    }
    
    public VCFDataLineException(String msg, Throwable cause)
    {
        super(msg, cause);
    } 
}
