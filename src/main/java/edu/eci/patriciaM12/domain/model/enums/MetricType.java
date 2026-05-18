package edu.eci.patriciaM12.domain.model.enums;

/**
 * Identifies the dimension or category of data that an analytics query or panel
 * should aggregate.  Used by the admin analytics use case to filter the type of
 * metric returned in the dashboard response.
 */
public enum MetricType {

    /** Metric related to registered or active users. */
    USERS,

    /** Metric related to the number of patches (activities). */
    PARCHES,

    /** Metric related to platform events (joins, views, etc.). */
    EVENTS,

    /** Metric related to user-to-patch matches or recommendations. */
    MATCHES,

    /** Metric related to campus zone usage distribution. */
    ZONES
}
