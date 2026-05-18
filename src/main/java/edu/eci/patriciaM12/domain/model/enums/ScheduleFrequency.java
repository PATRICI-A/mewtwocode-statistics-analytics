package edu.eci.patriciaM12.domain.model.enums;

/**
 * Defines how often a scheduled report should be automatically regenerated and delivered
 * to the configured recipient (RF-19 RN-19.6).
 *
 * <ul>
 *   <li>{@link #DAILY} — report is generated every day</li>
 *   <li>{@link #WEEKLY} — report is generated once per week</li>
 *   <li>{@link #MONTHLY} — report is generated once per month</li>
 * </ul>
 */
public enum ScheduleFrequency {

    /** Report regenerated and delivered every calendar day. */
    DAILY,

    /** Report regenerated and delivered once per calendar week. */
    WEEKLY,

    /** Report regenerated and delivered once per calendar month. */
    MONTHLY
}
