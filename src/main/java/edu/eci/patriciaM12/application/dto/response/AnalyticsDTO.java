package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.eci.patriciaM12.domain.model.CategoryStat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Generic analytics data transfer object that aggregates time-series counts,
 * a totals summary, and an optional breakdown by category.
 * Empty collections and {@code null} values are excluded from serialisation.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(
        name = "Analytics",
        description = """
                Generic analytics data structure that aggregates time-series counts, total summary, \
                and optional breakdown by category. Used across multiple metric types in the admin \
                analytics panel."""
)
public class AnalyticsDTO {

    @Schema(
            description = "Time-series mapping of dates to metric values",
            example = "{\"2025-02-01\": 150, \"2025-02-02\": 162, \"2025-02-03\": 158}"
    )
    Map<LocalDate, Integer> timeSeries;

    @Schema(
            description = "Total aggregated value across the entire period",
            example = "3240"
    )
    Integer total;

    @Schema(
            description = "Breakdown of metric values by category",
            example = "[{\"category\": \"ACADEMIC\", \"count\": 45}, {\"category\": \"SPORTS\", \"count\": 32}]"
    )
    List<CategoryStat> categories;
}