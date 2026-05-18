package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import lombok.Builder;
import lombok.Value;

import java.time.DayOfWeek;

/**
 * Top-level response returned by the interaction analytics endpoint (RF-39).
 * <p>
 * Aggregates interaction metrics for the authenticated student: total interaction count,
 * the campus zone where they are most active, and the day of the week when activity peaks.
 * Fields are omitted from the JSON response when {@code null}.
 * </p>
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InteractionAnalyticsResponse {

    /** Total number of recorded social interactions in the observed period. */
    Integer totalInteractions;

    /** Campus zone in which the student generated the most interactions. */
    CampusZone mostActiveZone;

    /** Day of the week on which the student's interaction activity is highest. */
    DayOfWeek peakActivityDay;

    /**
     * Breakdown of interaction counts by type (e.g., parche attendance, RSVP, connection request).
     * Keyed by interaction type name; values are non-negative counts.
     */
    java.util.Map<String, Integer> interactionSummary;
}
