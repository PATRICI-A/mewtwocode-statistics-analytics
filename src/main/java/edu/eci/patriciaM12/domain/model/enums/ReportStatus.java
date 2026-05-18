package edu.eci.patriciaM12.domain.model.enums;

/**
 * Tracks the lifecycle state of an asynchronous report generation request
 * ({@link edu.eci.patriciaM12.domain.model.ReportRequest}).
 */
public enum ReportStatus {

    /** The report has been accepted and is queued or currently being generated. */
    PENDING,

    /** The report has been successfully generated and its file URL is available. */
    READY,

    /** The report generation process encountered an error and could not be completed. */
    FAILED
}