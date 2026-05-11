package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.List;

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
