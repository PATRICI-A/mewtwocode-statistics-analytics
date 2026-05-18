package edu.eci.patriciaM12.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Alternative date-range wrapper used in report filter requests when the caller wants to
 * express the range as a nested object rather than flat {@code dateFrom}/{@code dateTo}
 * parameters (RF-19).
 * <p>
 * When {@link ReportFiltersRequest#getDateRange()} is populated, the report service uses
 * this object's dates instead of the top-level {@code dateFrom}/{@code dateTo} fields.
 * </p>
 */
@Data
@NoArgsConstructor
public class DateRangeDTO {

    /** Inclusive start of the reporting period. Must not be {@code null}. */
    @NotNull
    private LocalDate startDate;

    /** Inclusive end of the reporting period. Must not be {@code null}. */
    @NotNull
    private LocalDate endDate;
}
