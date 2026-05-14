package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Response payload returned by the admin analytics panel endpoint.
 * Each field is optional and is only serialised when it is not {@code null}, following the
 * metric-type filter applied at query time.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminAnalyticsResponse {
    AnalyticsDTO activeUsers;
    AnalyticsDTO parcheStats;
    List<EventAnalyticsDTO> topEvents;
    Double matchSuccessRate;
    HeatmapDTO campusHeatmap;
}
