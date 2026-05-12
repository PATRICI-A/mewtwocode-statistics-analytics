package edu.eci.patriciaM12.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidReportFiltersException extends RuntimeException {
    public InvalidReportFiltersException(String message) {
        super(message);
    }
}
