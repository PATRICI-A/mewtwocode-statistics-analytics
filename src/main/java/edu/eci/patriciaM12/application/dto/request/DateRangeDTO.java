package edu.eci.patriciaM12.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "DateRange",
        description = """
                Alternative date-range wrapper for report filters. Takes precedence over the \
                top-level `dateFrom`/`dateTo` fields when both are provided. Enables callers \
                to express the date boundary as a nested object instead of flat parameters."""
)
public class DateRangeDTO {

    @NotNull
    @Schema(
            description = "Inclusive start date of the reporting period in ISO-8601 format (yyyy-MM-dd)",
            example = "2025-02-01",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate startDate;

    @NotNull
    @Schema(
            description = "Inclusive end date of the reporting period in ISO-8601 format (yyyy-MM-dd). Must not be before startDate.",
            example = "2025-06-30",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate endDate;
}