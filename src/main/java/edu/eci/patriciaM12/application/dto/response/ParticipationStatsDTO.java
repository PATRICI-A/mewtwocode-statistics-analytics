package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.ParticipationTrend;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "ParticipationStats",
        description = """
                Aggregated institutional-level student participation statistics (RF-40 RN-40.2). \
                Provides a complete view of student engagement across the campus, including \
                active student counts, attendance figures, and trend analysis."""
)
public class ParticipationStatsDTO {

    @Schema(
            description = "Total number of distinct students who attended at least one parche in the period",
            example = "1240"
    )
    Integer totalActiveStudents;

    @Schema(
            description = "Cumulative number of parche attendances across all students in the period",
            example = "3450"
    )
    Integer totalParchesAttended;

    @Schema(
            description = "Total number of event RSVPs confirmed by students in the period",
            example = "890"
    )
    Integer totalRsvpConfirmed;

    @Schema(
            description = """
                    Directional trend of student participation relative to the previous equivalent period. \
                    Possible values: `GROWING` (increase > 5%), `DECLINING` (decrease > 5%), `STABLE` (otherwise).""",
            example = "GROWING"
    )
    ParticipationTrend participationTrend;
}