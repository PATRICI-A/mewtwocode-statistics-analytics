package edu.eci.patriciaM12.domain.exceptions;

import java.util.UUID;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(UUID id) {
        super("Reporte no encontrado: " + id);
    }
}