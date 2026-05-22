package edu.eci.patriciaM12.application.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO published when an async CSV report finishes generating successfully.
 *
 * <p>Published to {@code statistics.exchange} with routing key {@code report.ready}.
 * The notification service must add a consumer for this routing key to deliver
 * a {@code REPORT_READY} push notification to the requesting user.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportReadyEventDto {

    /** UUID of the user who requested the report and will receive the notification. */
    private UUID requestedBy;

    /** UUID of the finished report request. */
    private UUID reportId;

    /** Download URL of the generated CSV file. */
    private String downloadUrl;

    /** Timestamp at which the report finished generating. */
    private LocalDateTime timestamp;
}
