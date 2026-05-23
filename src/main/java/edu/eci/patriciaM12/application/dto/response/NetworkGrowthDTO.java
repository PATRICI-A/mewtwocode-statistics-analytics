package edu.eci.patriciaM12.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Measures the growth of a student's social network between two consecutive weeks (RF-38 RN-38.3).
 * <p>
 * The {@code growthRate} is expressed as a percentage: a positive value indicates network
 * expansion, zero indicates stagnation, and a negative value indicates contraction.
 * </p>
 */
@Value
@Builder
@Schema(
        name = "NetworkGrowth",
        description = """
                Measures the growth of a student's social network between two consecutive weeks (RF-38 RN-38.3). \
                Growth rate is expressed as a percentage: positive = expansion, zero = stagnation, \
                negative = contraction."""
)
public class NetworkGrowthDTO {

    @Schema(
            description = "Number of active peer connections the student had during the current week",
            example = "24"
    )
    Integer currentWeekConnections;

    @Schema(
            description = "Number of active peer connections the student had during the previous week",
            example = "18"
    )
    Integer previousWeekConnections;

    @Schema(
            description = """
                    Percentage change in peer connections from the previous week to the current week. \
                    Computed as `(current - previous) / previous * 100`. `null` when previousWeekConnections is zero.""",
            example = "33.33"
    )
    Double growthRate;
}