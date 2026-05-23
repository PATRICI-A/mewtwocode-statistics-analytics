package edu.eci.patriciaM12.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Data transfer object representing a metric alert generated when a key indicator
 * drops more than 30 % relative to the previous week (RF-18 RN-18.6).
 * <p>
 * Alerts are computed by {@code AdminAnalyticsService} and returned inside
 * {@link AdminAnalyticsResponse#getAlerts()}.  Each alert carries the name of the
 * affected metric, the percentage decrease, and a human-readable message intended
 * for the administrator dashboard.
 * </p>
 */
@Value
@Builder
@Schema(
        name = "Alert",
        description = """
                Metric alert generated when a key indicator drops more than 30% relative to the \
                previous week (RF-18 RN-18.6). Displayed on the administrator dashboard to highlight \
                concerning trends that may require attention."""
)
public class AlertDTO {

    @Schema(
            description = "Machine-readable name of the metric that triggered the alert",
            example = "activeUsers"
    )
    String metricName;

    @Schema(
            description = "Percentage decrease relative to the same metric measured in the previous week. Always a positive value.",
            example = "35.0",
            minimum = "0"
    )
    Double dropPercentage;

    @Schema(
            description = "Human-readable description of the alert intended for display on the administrator panel",
            example = "Active users dropped by 35% compared to last week"
    )
    String message;
}