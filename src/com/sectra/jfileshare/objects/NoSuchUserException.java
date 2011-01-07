package com.sectra.jfileshare.objects;

/**
 * User that does not exist in database has been requested
 * @author markus
 */
public class NoSuchUserException extends Exception {

    /**
     * Creates a new instance of <code>NoSuchUserException</code> without detail message.
     */
    public NoSuchUserException() {
    }


    /**
     * Constructs an instance of <code>NoSuchUserException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchUserException(String msg) {
        super(msg);
    }
}
