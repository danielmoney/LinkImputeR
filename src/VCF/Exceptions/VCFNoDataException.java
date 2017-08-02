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
public class VCFNoDataException extends Exception
{

    /**
     * Creates a new instance of <code>VCFNoDataException</code> without detail
     * message.
     */
    public VCFNoDataException()
    {
    }

    /**
     * Constructs an instance of <code>VCFNoDataException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public VCFNoDataException(String msg)
    {
        super(msg);
    }
}
