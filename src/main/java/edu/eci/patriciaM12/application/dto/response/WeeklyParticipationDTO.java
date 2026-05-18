package edu.eci.patriciaM12.application.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Aggregates a student's participation activity for a specific week (RF-38 RN-38.2).
 * <p>
 * Returned as part of {@link SocialIndicatorsResponse} and populated by
 * {@code SocialIndicatorsService} from {@code StudentMetricsRepositoryPort} weekly queries.
 * </p>
 */
@Value
@Builder
public class WeeklyParticipationDTO {

    /** Number of parches (social events) the student attended during the week. */
    Integer parcheCount;

    /** Number of event RSVPs the student confirmed during the week. */
    Integer eventRsvpCount;

    /** Number of peer connections the student was actively engaged with during the week. */
    Integer activeConnections;
}
