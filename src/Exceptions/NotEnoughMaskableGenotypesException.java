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
public class NotEnoughMaskableGenotypesException extends AlgorithmException
{

    /**
     * Creates a new instance of
     * <code>NotEnoughMaskableGenotypesException</code> without detail message.
     */
    public NotEnoughMaskableGenotypesException()
    {
    }

    /**
     * Constructs an instance of
     * <code>NotEnoughMaskableGenotypesException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public NotEnoughMaskableGenotypesException(String msg)
    {
        super(msg);
    }
}
