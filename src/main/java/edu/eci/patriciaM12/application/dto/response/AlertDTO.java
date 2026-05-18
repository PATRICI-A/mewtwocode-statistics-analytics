package edu.eci.patriciaM12.application.dto.response;

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
public class AlertDTO {

    /**
     * Machine-readable name of the metric that triggered the alert
     * (e.g., {@code "activeUsers"}, {@code "totalPatches"}).
     */
    String metricName;

    /**
     * Percentage decrease relative to the same metric measured in the previous week.
     * Always a positive value (e.g., {@code 35.0} means a 35 % drop).
     */
    Double dropPercentage;

    /**
     * Human-readable description of the alert intended for display on the administrator panel.
     */
    String message;
}
