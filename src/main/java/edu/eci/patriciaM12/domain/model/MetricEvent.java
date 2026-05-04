package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.MetricEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class MetricEvent {

    private UUID eventId;
    private String sourceModule;
    private MetricEventType eventType;
    private Map<String, Object> payload;
    private LocalDateTime emittedAt;
}

