package edu.eci.patriciaM12.domain.exceptions;

/**
 * Unchecked exception thrown when the CSV report generation process fails.
 * Typically raised by the {@code CsvGeneratorPort} implementation when an I/O
 * error or data-formatting problem prevents the file from being produced.
 */
public class CsvGenerationException extends RuntimeException {

    /**
     * Constructs a new {@code CsvGenerationException} with the given detail message.
     *
     * @param message human-readable description of the generation failure
     */
    public CsvGenerationException(String message) {
        super(message);
    }
}