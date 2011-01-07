package com.sectra.jfileshare.objects;

/**
 * File that does not exist in database has been requested
 * @author markus
 */
public class NoSuchFileException extends Exception {

    /**
     * Creates a new instance of <code>NoSuchFileException</code> without detail message.
     */
    public NoSuchFileException() {
    }


    /**
     * Constructs an instance of <code>NoSuchFileException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchFileException(String msg) {
        super(msg);
    }
}
