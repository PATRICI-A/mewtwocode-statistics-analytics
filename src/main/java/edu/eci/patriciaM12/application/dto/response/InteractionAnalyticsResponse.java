package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.DayOfWeek;
import java.util.Map;

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
@Schema(
        name = "InteractionAnalyticsResponse",
        description = """
                Aggregated interaction analytics for an authenticated student (RF-39). Provides \
                total interaction count, most-active campus zone, peak activity day, and a \
                breakdown of interactions by type. Implements the Empty Object Pattern — returns \
                zeroed-out values when no interactions have been recorded."""
)
public class InteractionAnalyticsResponse {

    @Schema(
            description = "Total number of recorded social interactions in the observed period",
            example = "157"
    )
    Integer totalInteractions;

    @Schema(
            description = "Campus zone in which the student generated the most interactions",
            example = "BIBLIOTECA"
    )
    CampusZone mostActiveZone;

    @Schema(
            description = "Day of the week on which the student's interaction activity is highest",
            example = "WEDNESDAY"
    )
    DayOfWeek peakActivityDay;

    @Schema(
            description = """
                    Breakdown of interaction counts by type (e.g., parche attendance, RSVP, connection request). \
                    Keys are interaction type names, values are non-negative counts.""",
            example = "{\"PARCHE_JOIN\": 42, \"EVENT_RSVP\": 28, \"CONNECTION_REQUEST\": 15}"
    )
    Map<String, Integer> interactionSummary;
}