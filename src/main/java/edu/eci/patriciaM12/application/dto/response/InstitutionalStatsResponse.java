package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

/**
 * Top-level response for the institutional statistics endpoint accessible to Bienestar users (RF-40).
 * <p>
 * Contains event/parche lifecycle statistics, student participation figures, and a composite
 * social activity index.  Fields are omitted from the JSON response when {@code null}, which
 * happens when the caller filters by a specific {@code InstitutionalMetricType}.
 * </p>
 *
 * <p>
 * The {@code socialActivityIndex} is computed as:
 * {@code parche_attendance×0.40 + connections×0.35 + event_rsvp×0.25}, normalised to [0.0, 1.0].
 * </p>
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstitutionalStatsResponse {

    /** Parche/event creation and lifecycle state counts. */
    EventsStatsDTO eventsStats;

    /** Student attendance, RSVP, and participation trend. */
    ParticipationStatsDTO participationStats;

    /**
     * Composite social activity index for the campus (RN-40.3).
     * Formula: {@code parche×0.40 + connections×0.35 + events×0.25}.
     * Normalised to [0.0, 1.0].
     */
    Double socialActivityIndex;
}
