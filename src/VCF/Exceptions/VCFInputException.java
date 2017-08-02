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
public class VCFInputException extends Exception
{

    /**
     * Creates a new instance of <code>VCFInputException</code> without detail
     * message.
     */
    public VCFInputException()
    {
    }

    /**
     * Constructs an instance of <code>VCFInputException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public VCFInputException(String msg)
    {
        super(msg);
    }
    
    public VCFInputException(String msg, int lineNumber)
    {
        super(msg);
    }
    
    public VCFInputException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    public VCFInputException(String msg, int lineNumber, Throwable cause)
    {
        super(msg, cause);
    }
}
