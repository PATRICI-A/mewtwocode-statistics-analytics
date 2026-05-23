package edu.eci.patriciaM12.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "WeeklyParticipation",
        description = """
                Aggregates a student's participation activity for a specific week (RF-38 RN-38.2). \
                Provides counts for parche attendance, event RSVPs, and active peer connections."""
)
public class WeeklyParticipationDTO {

    @Schema(
            description = "Number of parches (social events) the student attended during the week",
            example = "5"
    )
    Integer parcheCount;

    @Schema(
            description = "Number of event RSVPs the student confirmed during the week",
            example = "3"
    )
    Integer eventRsvpCount;

    @Schema(
            description = "Number of peer connections the student was actively engaged with during the week",
            example = "12"
    )
    Integer activeConnections;
}