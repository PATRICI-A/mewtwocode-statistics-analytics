package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "ReportRequestResponse",
        description = """
                Response payload returned after requesting a CSV report. Clients use this response \
                to track the report lifecycle via polling: `PENDING` → `READY` or `FAILED`. \
                For preview mode (`preview: true`), the status is immediately `READY` and `fileUrl` \
                contains the ephemeral preview URL."""
)
public class ReportRequestResponse {

    @Schema(
            description = "Unique identifier of the report request. Use this ID when polling GET /reports/{id}/download",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID id;

    @Schema(
            description = """
                    Current status of the report generation process:
                    - `PENDING` — queued, generation in progress (poll again)
                    - `READY` — generation complete, file available at `fileUrl`
                    - `FAILED` — an unrecoverable error occurred, request a new report""",
            example = "PENDING"
    )
    ReportStatus status;

    @Schema(
            description = """
                    Signed download URL of the generated report. Present only when `status` is `READY`. \
                    For preview mode (`preview: true`), this URL provides immediate access to the \
                    10-row preview. For standard reports, the URL is valid for a limited time window.""",
            example = "https://storage.eci.edu.co/reports/abc123.csv?token=eyJhbGciOiJIUzI1NiIs..."
    )
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