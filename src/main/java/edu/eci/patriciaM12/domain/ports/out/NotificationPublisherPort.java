package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.application.dto.event.ReportReadyEventDto;

/**
 * Output port that abstracts publishing notification events to the
 * notification service via the message broker.
 *
 * <p>Implementations are infrastructure adapters; domain services depend only
 * on this interface, preserving hexagonal architecture boundaries.</p>
 */
public interface NotificationPublisherPort {

    /**
     * Publishes a report-ready event so the notification service can send
     * a push notification to the user who requested the report.
     *
     * <p>Fire-and-forget; failures are logged but must not propagate to
     * the caller so that a broker outage does not break the report flow.</p>
     *
     * @param event the report ready event; must not be {@code null}
     */
    void publishReportReady(ReportReadyEventDto event);
}
