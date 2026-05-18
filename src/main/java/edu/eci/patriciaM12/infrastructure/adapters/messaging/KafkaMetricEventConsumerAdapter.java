package edu.eci.patriciaM12.infrastructure.adapters.messaging;

import edu.eci.patriciaM12.domain.model.MetricEvent;
import edu.eci.patriciaM12.domain.model.enums.MetricEventType;
import edu.eci.patriciaM12.domain.ports.in.ProcessMetricEventUseCase;
import edu.eci.patriciaM12.infrastructure.adapters.messaging.dto.InboundPatchEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka consumer adapter that listens to the {@code m06-patch-events} topic and delegates
 * each received message to {@link ProcessMetricEventUseCase}.
 * <p>
 * This class is the only component in M12 that is aware of Kafka.  It converts the
 * infrastructure-level {@link InboundPatchEventMessage} DTO into the domain
 * {@link MetricEvent} model, preserving the hexagonal architecture boundary.
 * </p>
 *
 * <p><strong>Topic:</strong> {@code m06-patch-events} (value from {@code m12.kafka.topic.patch-events})<br>
 * <strong>Group:</strong> {@code m12-analytics-group}<br>
 * <strong>Container factory:</strong> {@code patchEventListenerContainerFactory}
 * (defined in {@link edu.eci.patriciaM12.infrastructure.config.KafkaConsumerConfig})</p>
 *
 * <p>Exceptions thrown by the use case are caught and logged at ERROR level so that the
 * consumer offset advances and the broker is not flooded with retries for poison messages.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMetricEventConsumerAdapter {

    private final ProcessMetricEventUseCase processMetricEventUseCase;

    /**
     * Receives a {@link InboundPatchEventMessage} from Kafka, converts it to a domain
     * {@link MetricEvent}, and delegates processing to the use case.
     *
     * @param message the deserialized inbound message; never {@code null} under normal operation
     */
    @KafkaListener(
            topics = "${m12.kafka.topic.patch-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "patchEventListenerContainerFactory"
    )
    public void onPatchEvent(InboundPatchEventMessage message) {
        log.debug("Received patch event from Kafka: eventId={} type={} userId={}",
                message.getEventId(), message.getEventType(), message.getUserId());
        try {
            MetricEvent domain = toDomain(message);
            processMetricEventUseCase.process(domain);
        } catch (Exception e) {
            log.error("Failed to process patch event eventId={}: {}", message.getEventId(), e.getMessage(), e);
        }
    }

    /**
     * Converts an {@link InboundPatchEventMessage} (infrastructure DTO) into a domain
     * {@link MetricEvent}.
     * <p>
     * The payload map carries all event-specific fields (userId, patchId, patchCategory,
     * campusZone) as string keys so that the domain model remains framework-agnostic.
     * Unknown {@code eventType} values are mapped to {@code null}; the processor will log
     * a warning and skip those events.
     * </p>
     *
     * @param msg the inbound message to convert
     * @return the equivalent domain {@link MetricEvent}
     */
    private MetricEvent toDomain(InboundPatchEventMessage msg) {
        Map<String, Object> payload = new HashMap<>();
        if (msg.getUserId() != null)       payload.put("userId",       msg.getUserId().toString());
        if (msg.getPatchId() != null)      payload.put("patchId",      msg.getPatchId().toString());
        if (msg.getPatchCategory() != null) payload.put("patchCategory", msg.getPatchCategory().name());
        if (msg.getCampusZone() != null)   payload.put("campusZone",   msg.getCampusZone());

        MetricEventType eventType = parseEventType(msg.getEventType());

        return MetricEvent.builder()
                .eventId(msg.getEventId() != null ? msg.getEventId() : UUID.randomUUID())
                .sourceModule(msg.getSourceModule())
                .eventType(eventType)
                .payload(payload)
                .emittedAt(msg.getEmittedAt() != null ? msg.getEmittedAt() : LocalDateTime.now())
                .build();
    }

    /**
     * Parses the string event type from M06 into a {@link MetricEventType} enum value.
     * Returns {@code null} for unrecognised strings, which causes the processor to log a warning.
     *
     * @param raw the raw event type string from the Kafka message (e.g. {@code "JOIN"})
     * @return the corresponding {@link MetricEventType}, or {@code null} if not recognised
     */
    private MetricEventType parseEventType(String raw) {
        if (raw == null) return null;
        return switch (raw.toUpperCase()) {
            case "JOIN"           -> MetricEventType.JOIN;
            case "VIEW"           -> MetricEventType.VIEW;
            case "SKIP"           -> MetricEventType.LEAVE;
            case "CREATE"         -> MetricEventType.CREATE;
            case "DELETE", "STATUS_CHANGED" -> MetricEventType.DELETE;
            default -> {
                log.warn("Unrecognised event type from Kafka: '{}'", raw);
                yield null;
            }
        };
    }
}
