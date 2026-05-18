package edu.eci.patriciaM12.domain.model.enums;

/**
 * Describes the directional trend of student participation over the observed period.
 * Computed by comparing the current period's active-student count with the equivalent
 * prior period in the institutional statistics service (RF-40).
 *
 * <ul>
 *   <li>{@link #GROWING} — participation increased by more than 5 % vs. the prior period</li>
 *   <li>{@link #STABLE} — participation change is within ±5 % of the prior period</li>
 *   <li>{@link #DECLINING} — participation decreased by more than 5 % vs. the prior period</li>
 * </ul>
 */
public enum ParticipationTrend {

    /** Participation rose more than 5 % compared to the previous equivalent period. */
    GROWING,

    /** Participation remained within ±5 % of the previous equivalent period. */
    STABLE,

    /** Participation fell more than 5 % compared to the previous equivalent period. */
    DECLINING
}
