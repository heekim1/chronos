package com.roche.rss.lannister.exception;

/**
 * A customised exception for invalid tasks defined in config-file
 */
public class InvalidTaskException extends Exception {
    public InvalidTaskException(String message) {
        super(message);
    }
}
