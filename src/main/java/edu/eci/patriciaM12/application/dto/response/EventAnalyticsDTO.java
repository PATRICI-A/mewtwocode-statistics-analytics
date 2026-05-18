package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
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
public class EventAnalyticsDTO {

    /** Unique identifier of the parche/event as stored in M06 Feed & Search service. */
    String eventId;

    /** Display name of the parche/event. */
    String eventName;

    /** Number of confirmed RSVPs recorded for this event in the observed period. */
    Integer rsvpCount;

    /** Position of this event in the top-events ranking (1 = highest RSVP count). */
    Integer rank;

    /** Category of the parche, derived from M06's {@code PatchCategory} enum. */
    PatchCategory category;
}
