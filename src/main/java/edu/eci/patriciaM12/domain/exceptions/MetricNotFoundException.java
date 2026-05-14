package edu.eci.patriciaM12.domain.exceptions;

/**
 * Unchecked exception thrown when a requested metric record cannot be found
 * in the persistence layer.  Callers should map this exception to an HTTP 404
 * Not Found response at the web adapter layer.
 */
public class MetricNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code MetricNotFoundException} with the given detail message.
     *
     * @param message human-readable description identifying the missing metric
     */
    public MetricNotFoundException(String message) {
        super(message);
    }
}