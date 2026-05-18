package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.MetricType;
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
public class ReportHistoryDTO {

    /** Unique identifier of the report request. */
    UUID reportId;

    /** Timestamp at which the report file was successfully generated. */
    LocalDateTime generatedAt;

    /**
     * Metric dimensions included in this report.
     * {@code null} or empty means all metrics were included.
     */
    List<MetricType> metrics;

    /** URL from which the generated CSV file can be downloaded while still valid. */
    String downloadUrl;

    /** Timestamp after which the file is automatically purged (30 days from {@code generatedAt}). */
    LocalDateTime expiresAt;
}
