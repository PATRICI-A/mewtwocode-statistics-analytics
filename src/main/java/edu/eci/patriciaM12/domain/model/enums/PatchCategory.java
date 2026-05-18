package edu.eci.patriciaM12.domain.model.enums;

/**
 * Classifies the thematic category of a patch activity on the platform.
 * Categories drive both the analytics aggregations (e.g., top categories in
 * {@link edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot}) and the
 * filter options available for report generation.
 */
public enum PatchCategory {

    /** Academic study session or tutoring activity. */
    STUDY,

    /** Physical sport or outdoor recreational activity. */
    SPORTS,

    /** Cultural or artistic activity. */
    CULTURE,

    /** Video game or board game session. */
    GAMING,

    /** Food, cooking, or dining-related activity. */
    FOOD,

    /** Music performance, rehearsal, or listening activity. */
    MUSIC,

    /** Any activity that does not fit the above categories. */
    OTHER
}