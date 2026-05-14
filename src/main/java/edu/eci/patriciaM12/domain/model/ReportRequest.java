package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Represents an asynchronous request to generate an analytics report.
 * A report request is created by a user, processed in the background, and
 * transitions through the {@link ReportStatus} lifecycle
 * ({@code PENDING} → {@code READY} or {@code FAILED}).
 *
 * <p>Once the report is ready, {@link #getFileUrl()} returns the URL where the
 * generated CSV file can be downloaded.</p>
 */
@Getter
@Builder
public class ReportRequest {

    /** Unique identifier for this report request. */
    private UUID id;

    /** Identifier of the user who submitted this report request. */
    private UUID requestedBy;

    /** Inclusive start date of the data range covered by the report. */
    private LocalDate dateFrom;

    /** Inclusive end date of the data range covered by the report. */
    private LocalDate dateTo;

    /** Filter criteria applied when generating the report content. */
    private ReportFilters filters;

    /** Current processing state of this report request. */
    private ReportStatus status;

    /**
     * URL pointing to the generated report file.
     * This field is {@code null} while the report is in {@code PENDING} state
     * and is populated once the status transitions to {@code READY}.
     */
    private String fileUrl;

}