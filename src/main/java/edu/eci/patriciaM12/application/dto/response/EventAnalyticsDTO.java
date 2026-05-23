package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Data transfer object representing analytics for a single event (parche) returned in the
 * admin analytics panel (RF-18 RN-18.3).
 * <p>
 * Events are ranked by RSVP count and included in the {@code topEvents} list of
 * {@link AdminAnalyticsResponse}. The {@code rank} field reflects the event's position in
 * that ordered list (1 = most popular).
 * </p>
 */
@Value
@Builder
@Schema(
        name = "EventAnalytics",
        description = """
                Analytics for a single event (parche) in the admin analytics panel (RF-18 RN-18.3). \
                Events are ranked by RSVP count; the `rank` field indicates position (1 = most popular)."""
)
public class EventAnalyticsDTO {

    @Schema(
            description = "Unique identifier of the parche/event as stored in M06 Feed & Search service",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    String eventId;

    @Schema(
            description = "Display name of the parche/event",
            example = "International AI Conference 2025"
    )
    String eventName;

    @Schema(
            description = "Number of confirmed RSVPs recorded for this event in the observed period",
            example = "87"
    )
    Integer rsvpCount;

    @Schema(
            description = "Position of this event in the top-events ranking (1 = highest RSVP count)",
            example = "3"
    )
    Integer rank;

    @Schema(
            description = "Category of the parche",
            example = "ACADEMIC"
    )
    PatchCategory category;
}