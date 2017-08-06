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
public class AlgorithmException extends Exception
{

    /**
     * Creates a new instance of <code>AlgorithmException</code> without detail
     * message.
     */
    public AlgorithmException()
    {
    }

    /**
     * Constructs an instance of <code>AlgorithmException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public AlgorithmException(String msg)
    {
        super(msg);
    }
}
