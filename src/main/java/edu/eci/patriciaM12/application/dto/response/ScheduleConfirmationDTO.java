package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.ScheduleFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Confirmation payload returned after a recurring report schedule has been registered (RF-19 RN-19.6).
 * <p>
 * Tells the caller the ID of the schedule, when the first delivery will occur, and to
 * which address the reports will be sent.
 * </p>
 */
@Value
@Builder
@Schema(
        name = "ScheduleConfirmation",
        description = """
                Confirmation payload returned after a recurring report schedule has been registered (RF-19 RN-19.6). \
                Provides the schedule ID, frequency, delivery email, and timestamp of the first scheduled delivery."""
)
public class ScheduleConfirmationDTO {

    @Schema(
            description = "Unique identifier of the registered schedule job",
            example = "660e8400-e29b-41d4-a716-446655440001"
    )
    UUID scheduleId;

    @Schema(
            description = "How often the report will be generated and delivered",
            example = "WEEKLY"
    )
    ScheduleFrequency frequency;

    @Schema(
            description = "E-mail address that will receive each report delivery",
            example = "admin@eci.edu.co"
    )
    String deliveryEmail;

    @Schema(
            description = "ISO-8601 timestamp of the first scheduled delivery",
            example = "2025-06-22T08:00:00.000Z"
    )
    LocalDateTime nextDeliveryAt;
}