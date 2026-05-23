package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Response payload returned by the admin analytics panel endpoint (RF-18).
 * <p>
 * Each field is optional and is only serialised when it is not {@code null}, following the
 * metric-type filter applied at query time.  The full response (no filter) includes all
 * nine fields below.
 * </p>
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "AdminAnalyticsResponse",
        description = """
                Comprehensive analytics dashboard for platform administrators (RF-18). Includes up to \
                nine metric categories depending on the applied filters. Fields are omitted from the \
                JSON response when filtered out via the `metricType` parameter. The full unfiltered \
                response contains all metrics described below."""
)
public class AdminAnalyticsResponse {

    @Schema(description = "Time-series of active-user counts across the requested period (RN-18.1)")
    AnalyticsDTO activeUsers;

    @Schema(description = "Aggregated parche counts grouped by category (RN-18.2)")
    AnalyticsDTO parcheStats;

    @Schema(description = "Ranked list of the top events by RSVP count in the period (RN-18.3)")
    List<EventAnalyticsDTO> topEvents;

    @Schema(
            description = "Percentage of M05 parche matches that resulted in confirmed attendance (RN-18.4)",
            example = "78.5",
            minimum = "0",
            maximum = "100"
    )
    Double matchSuccessRate;

    @Schema(description = "Hourly activity density map broken down by campus zone (RN-18.5)")
    HeatmapDTO campusHeatmap;

    @Schema(
            description = "Percentage of users who were active in both this period and the previous equivalent period (RN-18.7)",
            example = "65.2",
            minimum = "0",
            maximum = "100"
    )
    Double retentionRate;

    @Schema(
            description = "Number of parches that were abandoned (cancelled or never reached minimum membership) (RN-18.8)",
            example = "12"
    )
    Integer abandonedParches;

    @Schema(
            description = "Average time in minutes from parche creation until the first member joins (RN-18.9)",
            example = "45"
    )
    Long avgTimeToFirstMember;

    @Schema(
            description = "Alerts for metrics that dropped more than 30% relative to the previous week (RN-18.6)"
    )
    List<AlertDTO> alerts;
}