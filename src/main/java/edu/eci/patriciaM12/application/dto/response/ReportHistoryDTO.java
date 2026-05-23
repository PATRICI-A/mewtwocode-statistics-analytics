package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.MetricType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Summarises a previously generated report for use in the report history list (RF-19 RN-19.5).
 * <p>
 * Generated reports are retained for 30 days; after that the download URL becomes invalid.
 * The {@code expiresAt} field tells the client exactly when the file will be purged.
 * </p>
 */
@Value
@Builder
@Schema(
        name = "ReportHistory",
        description = """
                Summary of a previously generated report for the report history list (RF-19 RN-19.5). \
                Reports are retained for 30 days from generation; after that, the download URL becomes \
                invalid and the file is purged from storage. The `expiresAt` field indicates the exact \
                purge timestamp."""
)
public class ReportHistoryDTO {

    @Schema(
            description = "Unique identifier of the report request",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID reportId;

    @Schema(
            description = "ISO-8601 timestamp of when the report file was successfully generated",
            example = "2025-06-15T14:30:00.000Z"
    )
    LocalDateTime generatedAt;

    @Schema(
            description = """
                    Metric dimensions included in this report. Possible values: `USERS`, `PARCHES`, \
                    `EVENTS`, `MATCHES`, `ZONES`. `null` or empty list means all metrics were included.""",
            example = "[\"USERS\", \"PARCHES\"]"
    )
    List<MetricType> metrics;

    @Schema(
            description = "Signed download URL of the generated CSV file. Valid until `expiresAt`.",
            example = "https://storage.eci.edu.co/reports/abc123.csv?token=eyJhbGciOiJIUzI1NiIs..."
    )
    String downloadUrl;

    @Schema(
            description = "ISO-8601 timestamp after which the file is automatically purged from storage (30 days from `generatedAt`)",
            example = "2025-07-15T14:30:00.000Z"
    )
    LocalDateTime expiresAt;
}