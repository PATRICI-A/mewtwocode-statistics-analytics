package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.domain.model.MetricEvent;

/**
 * Input port (primary port) that defines the use case for processing inbound metric
 * events received from the Kafka event stream.  The Kafka consumer adapter calls this
 * port for every message it deserializes, delegating business logic to the application layer.
 */
public interface ProcessMetricEventUseCase {

    /**
     * Processes a single {@link MetricEvent}, updating the relevant analytics aggregates
     * (student metrics, admin snapshot) based on the event type and payload.
     *
     * @param event the metric event to process; must not be {@code null}
     */
    void process(MetricEvent event);
}