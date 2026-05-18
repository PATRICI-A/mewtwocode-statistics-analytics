package edu.eci.patriciaM12.application.dto.response;

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
public class NetworkGrowthDTO {

    /** Number of active peer connections the student had during the current week. */
    Integer currentWeekConnections;

    /** Number of active peer connections the student had during the previous week. */
    Integer previousWeekConnections;

    /**
     * Percentage change in peer connections from the previous week to the current week.
     * Computed as {@code (current - previous) / previous * 100}; {@code null} when
     * {@code previousWeekConnections} is zero (no division by zero).
     */
    Double growthRate;
}
