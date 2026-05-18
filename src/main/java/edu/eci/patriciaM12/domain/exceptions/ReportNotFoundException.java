package edu.eci.patriciaM12.domain.exceptions;

import java.util.UUID;

/**
 * Unchecked exception thrown when a {@link edu.eci.patriciaM12.domain.model.ReportRequest}
 * with the requested identifier does not exist in the persistence layer.
 * Callers should map this exception to an HTTP 404 Not Found response at the web adapter layer.
 */
public class ReportNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code ReportNotFoundException} for the given report identifier.
     *
     * @param id the {@link UUID} of the report that could not be found
     */
    public ReportNotFoundException(UUID id) {
        super("Reporte no encontrado: " + id);
    }
}