package edu.eci.patriciaM12.domain.exceptions;

public class CsvGenerationException extends RuntimeException {
    public CsvGenerationException(String message) {
        super(message);
    }
}