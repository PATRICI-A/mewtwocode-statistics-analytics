package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.ScheduleFrequency;
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
public class ScheduleConfirmationDTO {

    /** Unique identifier of the registered schedule job. */
    UUID scheduleId;

    /** How often the report will be generated and delivered. */
    ScheduleFrequency frequency;

    /** E-mail address that will receive each report delivery. */
    String deliveryEmail;

    /** Timestamp of the first scheduled delivery. */
    LocalDateTime nextDeliveryAt;
}
