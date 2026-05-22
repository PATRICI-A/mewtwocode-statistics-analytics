package edu.eci.patriciaM12.infrastructure.adapters.adapter;

import edu.eci.patriciaM12.application.dto.event.ReportReadyEventDto;
import edu.eci.patriciaM12.domain.ports.out.NotificationPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ adapter that publishes notification events to the statistics exchange.
 *
 * <p>Messages are serialized as JSON via {@code Jackson2JsonMessageConverter}.
 * The notification service must bind a queue to {@code statistics.exchange} with
 * routing key {@code report.ready} to consume these events.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitNotificationPublisherAdapter implements NotificationPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.statistics:statistics.exchange}")
    private String statisticsExchange;

    @Value("${rabbitmq.routing-key.report-ready:report.ready}")
    private String reportReadyRoutingKey;

    @Override
    public void publishReportReady(ReportReadyEventDto event) {
        try {
            rabbitTemplate.convertAndSend(statisticsExchange, reportReadyRoutingKey, event);
            log.debug("Published report-ready event for user {} report {} to exchange '{}' rk '{}'",
                    event.getRequestedBy(), event.getReportId(),
                    statisticsExchange, reportReadyRoutingKey);
        } catch (Exception e) {
            log.warn("Failed to publish report-ready event for report {}: {}",
                    event.getReportId(), e.getMessage());
        }
    }
}
