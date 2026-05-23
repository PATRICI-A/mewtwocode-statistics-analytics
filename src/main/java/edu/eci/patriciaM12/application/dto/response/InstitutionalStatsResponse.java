package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Top-level response for the institutional statistics endpoint accessible to Bienestar users (RF-40).
 * <p>
 * Contains event/parche lifecycle statistics, student participation figures, and a composite
 * social activity index.  Fields are omitted from the JSON response when {@code null}, which
 * happens when the caller filters by a specific {@code InstitutionalMetricType}.
 * </p>
 * <p>
 * The {@code socialActivityIndex} is computed as:
 * {@code parche_attendance×0.40 + connections×0.35 + event_rsvp×0.25}, normalised to [0.0, 1.0].
 * </p>
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "InstitutionalStatsResponse",
        description = """
                Campus-wide aggregated statistics for Bienestar users (RF-40). Includes event lifecycle \
                metrics, student participation figures, and a composite social activity index. \
                Fields are omitted when filtered by `InstitutionalMetricType`. The `socialActivityIndex` \
                is computed as: `parche_attendance × 0.40 + connections × 0.35 + event_rsvp × 0.25`, \
                normalised to [0.0, 1.0] (RN-40.3)."""
)
public class InstitutionalStatsResponse {

    @Schema(description = "Parche/event creation and lifecycle state counts aggregated for the campus")
    EventsStatsDTO eventsStats;

    @Schema(description = "Student attendance, RSVP, and participation trend figures")
    ParticipationStatsDTO participationStats;

    @Schema(
            description = """
                    Composite social activity index for the campus (RN-40.3). \
                    Formula: `parche_attendance × 0.40 + connections × 0.35 + event_rsvp × 0.25`. \
                    Normalised to [0.0, 1.0] where higher values indicate greater campus social activity.""",
            example = "0.724",
            minimum = "0",
            maximum = "1"
    )
    Double socialActivityIndex;
}