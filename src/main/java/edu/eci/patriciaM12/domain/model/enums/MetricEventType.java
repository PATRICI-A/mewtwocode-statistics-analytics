package edu.eci.patriciaM12.domain.model.enums;

/**
 * Classifies the type of action captured by a {@link edu.eci.patriciaM12.domain.model.MetricEvent}
 * consumed from the Kafka event stream.  Each constant represents a distinct user
 * interaction with a patch on the platform.
 */
public enum MetricEventType {

    /** A user joined an existing patch. */
    JOIN,

    /** A user left a patch they had previously joined. */
    LEAVE,

    /** A user viewed the details of a patch without joining. */
    VIEW,

    /** A new patch was created by a user. */
    CREATE,

    /** An existing patch was deleted. */
    DELETE
}