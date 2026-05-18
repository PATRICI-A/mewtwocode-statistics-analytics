package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.ParticipationTrend;
import lombok.Builder;
import lombok.Value;

/**
 * Aggregates institutional-level student participation statistics (RF-40 RN-40.2).
 * <p>
 * Returned as part of {@link InstitutionalStatsResponse}.  The {@code participationTrend}
 * is computed by comparing the current period's active-student count with an equivalent
 * prior period: GROWING if increase > 5 %, DECLINING if decrease > 5 %, STABLE otherwise.
 * </p>
 */
@Value
@Builder
public class ParticipationStatsDTO {

    /** Total number of distinct students who attended at least one parche in the period. */
    Integer totalActiveStudents;

    /** Cumulative number of parche attendances across all students in the period. */
    Integer totalParchesAttended;

    /** Total number of event RSVPs confirmed by students in the period. */
    Integer totalRsvpConfirmed;

    /** Directional trend of student participation relative to the previous equivalent period. */
    ParticipationTrend participationTrend;
}
