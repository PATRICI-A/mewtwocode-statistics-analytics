package edu.eci.patriciaM12.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Unchecked exception thrown when the {@link edu.eci.patriciaM12.domain.model.ReportFilters}
 * provided by the caller contain invalid or logically inconsistent values (e.g., a
 * {@code dateFrom} that is after {@code dateTo}).  Spring MVC maps this exception to
 * an HTTP 400 Bad Request response via the {@code @ResponseStatus} annotation.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidReportFiltersException extends RuntimeException {

    /**
     * Constructs a new {@code InvalidReportFiltersException} with the given detail message.
     *
     * @param message human-readable description of the invalid filter condition
     */
    public InvalidReportFiltersException(String message) {
        super(message);
    }
}
