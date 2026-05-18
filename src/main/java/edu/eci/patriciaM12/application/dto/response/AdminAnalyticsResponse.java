package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
 *
 * <ul>
 *   <li>{@code activeUsers} — time-series and total for active user counts (RN-18.1)</li>
 *   <li>{@code parcheStats} — total and per-category parche counts (RN-18.2)</li>
 *   <li>{@code topEvents} — ranked list of the most-attended events (RN-18.3)</li>
 *   <li>{@code matchSuccessRate} — percentage of successful parche matches (RN-18.4)</li>
 *   <li>{@code campusHeatmap} — activity density per zone and hour (RN-18.5)</li>
 *   <li>{@code retentionRate} — percentage of users who returned in the period (RN-18.7)</li>
 *   <li>{@code abandonedParches} — count of parches cancelled or left without members (RN-18.8)</li>
 *   <li>{@code avgTimeToFirstMember} — average minutes until a parche gets its first member (RN-18.9)</li>
 *   <li>{@code alerts} — list of metrics that dropped more than 30 % vs. last week (RN-18.6)</li>
 * </ul>
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminAnalyticsResponse {

    /** Time-series of active-user counts across the requested period. */
    AnalyticsDTO activeUsers;

    /** Aggregated parche counts grouped by category. */
    AnalyticsDTO parcheStats;

    /** Ranked list of the top events by RSVP count in the period. */
    List<EventAnalyticsDTO> topEvents;

    /** Percentage of M05 parche matches that resulted in confirmed attendance. */
    Double matchSuccessRate;

    /** Hourly activity density map broken down by campus zone. */
    HeatmapDTO campusHeatmap;

    /** Percentage of users who were active in both this period and the previous equivalent period. */
    Double retentionRate;

    /** Number of parches that were abandoned (cancelled or never reached minimum membership). */
    Integer abandonedParches;

    /** Average time in minutes from parche creation until the first member joins. */
    Long avgTimeToFirstMember;

    /** Alerts for metrics that dropped more than 30 % relative to the previous week (RN-18.6). */
    List<AlertDTO> alerts;
}
