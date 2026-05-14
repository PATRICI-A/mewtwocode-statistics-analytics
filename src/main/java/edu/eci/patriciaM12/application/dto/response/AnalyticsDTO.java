package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.eci.patriciaM12.domain.model.CategoryStat;
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
public class AnalyticsDTO {
    Map<LocalDate, Integer> timeSeries;
    Integer total;
    List<CategoryStat> categories;
}
