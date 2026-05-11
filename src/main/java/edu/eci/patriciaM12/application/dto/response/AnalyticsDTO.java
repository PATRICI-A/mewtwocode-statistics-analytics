package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.eci.patriciaM12.domain.model.CategoryStat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AnalyticsDTO {
    Map<LocalDate, Integer> timeSeries;
    Integer total;
    List<CategoryStat> categories;
}
