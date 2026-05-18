package edu.eci.patriciaM12.application.dto.request;

import edu.eci.patriciaM12.domain.model.enums.ScheduleFrequency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Carries the scheduling configuration when requesting a recurring analytics report (RF-19 RN-19.6).
 * <p>
 * When this object is present in {@link ReportFiltersRequest#getSchedule()}, the report service
 * registers an automatic delivery job that regenerates and sends the report at the specified
 * frequency to the given e-mail address.
 * </p>
 */
@Data
@NoArgsConstructor
public class ScheduleDTO {

    /**
     * How often the report should be automatically regenerated and delivered.
     * Must not be {@code null} when scheduling is requested.
     */
    @NotNull
    private ScheduleFrequency frequency;

    /**
     * E-mail address to which the generated report file will be delivered.
     * Must be a syntactically valid e-mail address.
     */
    @Email
    @NotNull
    private String deliveryEmail;
}
