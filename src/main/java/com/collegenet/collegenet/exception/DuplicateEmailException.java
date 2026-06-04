package com.collegenet.collegenet.exception;

/**
 * Exception thrown when attempting to register with an email that is already in use.
 * This is a client error (400/409) not an authentication error (401).
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

