package edu.eci.patriciaM12.domain.model.enums;

/**
 * Represents the qualitative activity level of a student based on their weekly
 * participation metrics.  The level is computed by the social indicators service
 * from the combined score of parche attendance, RSVP confirmations, and active
 * connections within a given week.
 *
 * <ul>
 *   <li>{@link #LOW} — fewer than 2 activities in the week</li>
 *   <li>{@link #MEDIUM} — 2–4 activities in the week</li>
 *   <li>{@link #HIGH} — 5–9 activities in the week</li>
 *   <li>{@link #VERY_HIGH} — 10 or more activities in the week</li>
 * </ul>
 */
public enum ActivityLevel {

    /** Student participated in fewer than 2 social activities during the week. */
    LOW,

    /** Student participated in 2 to 4 social activities during the week. */
    MEDIUM,

    /** Student participated in 5 to 9 social activities during the week. */
    HIGH,

    /** Student participated in 10 or more social activities during the week. */
    VERY_HIGH
}
