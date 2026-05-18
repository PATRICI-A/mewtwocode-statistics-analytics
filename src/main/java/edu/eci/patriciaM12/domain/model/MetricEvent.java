package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.MetricEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Domain representation of a metric event consumed from the Kafka event stream.
 * Each event originates from another microservice (source module) and describes a
 * specific user interaction on the patch platform (e.g., joining or creating a patch).
 *
 * <p>Events are processed by
 * {@link edu.eci.patriciaM12.domain.ports.in.ProcessMetricEventUseCase} and used to
 * update student and admin analytics data.</p>
 */
@Getter
@Builder
public class MetricEvent {

    /** Unique identifier assigned to this event at the moment it was emitted. */
    private UUID eventId;

    /** Name of the microservice or module that produced this event. */
    private String sourceModule;

    /** Classifies the user action that triggered this event. */
    private MetricEventType eventType;

    /**
     * Arbitrary key-value pairs carrying event-specific data (e.g., patch ID,
     * user ID, category).  The set of keys depends on the {@code eventType}.
     */
    private Map<String, Object> payload;

    /** Timestamp at which the source module emitted this event to Kafka. */
    private LocalDateTime emittedAt;
}

