package edu.eci.patriciaM12.domain.model.enums;

/**
 * Defines the categories of institutional-level metrics that the Bienestar role
 * can request from the institutional statistics endpoint (RF-40).
 *
 * <ul>
 *   <li>{@link #EVENTS} — statistics about event (parche) creation and lifecycle</li>
 *   <li>{@link #PARTICIPATION} — student attendance and RSVP confirmation figures</li>
 *   <li>{@link #SOCIAL_ACTIVITY} — composite social activity index for the campus</li>
 *   <li>{@link #ALL} — all of the above categories in a single response</li>
 * </ul>
 */
public enum InstitutionalMetricType {

    /** Aggregated statistics about parche/event creation and lifecycle stages. */
    EVENTS,

    /** Aggregated student participation and RSVP confirmation figures. */
    PARTICIPATION,

    /** Composite index measuring social interaction across the campus (RF-40 RN-40.3). */
    SOCIAL_ACTIVITY,

    /** Returns all metric categories in a single institutional statistics response. */
    ALL
}
