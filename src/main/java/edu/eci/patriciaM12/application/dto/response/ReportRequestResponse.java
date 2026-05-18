package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Response payload returned after a CSV report is requested.
 * Clients poll this response to track the lifecycle of the report:
 * {@code PENDING} while queued, {@code READY} when the file URL is available,
 * or {@code FAILED} if generation encountered an error.
 */
@Value
@Builder
public class ReportRequestResponse {

    UUID id;
    ReportStatus status;
    String fileUrl;

    /**
     * Factory method that converts a domain {@link ReportRequest} into this response DTO.
     *
     * @param report the domain report request to convert
     * @return a new {@link ReportRequestResponse} reflecting the current state of the report
     */
    public static ReportRequestResponse from(ReportRequest report) {
        return ReportRequestResponse.builder()
                .id(report.getId())
                .status(report.getStatus())
                .fileUrl(report.getFileUrl())
                .build();
    }
}