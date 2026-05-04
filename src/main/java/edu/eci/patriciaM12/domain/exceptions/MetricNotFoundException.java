package edu.eci.patriciaM12.domain.exceptions;

public class MetricNotFoundException extends RuntimeException {
    public MetricNotFoundException(String message) {
        super(message);
    }
}