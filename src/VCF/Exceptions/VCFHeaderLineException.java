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
public class VCFHeaderLineException extends VCFInputException
{

    /**
     * Creates a new instance of <code>VCFHeaderLineException</code> without
     * detail message.
     */
    public VCFHeaderLineException()
    {
    }

    /**
     * Constructs an instance of <code>VCFHeaderLineException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public VCFHeaderLineException(String msg)
    {
        super(msg);
    }
    
    public VCFHeaderLineException(String msg, int lineNumber)
    {
        super(msg, lineNumber);
    }
    
    public VCFHeaderLineException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    public VCFHeaderLineException(String msg, int lineNumber, Throwable cause)
    {
        super(msg, lineNumber, cause);
    }
}
