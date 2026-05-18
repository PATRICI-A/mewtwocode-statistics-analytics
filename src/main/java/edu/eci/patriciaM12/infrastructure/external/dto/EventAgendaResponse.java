package edu.eci.patriciaM12.infrastructure.external.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventAgendaResponse(List<EventItem> events) {

    public record EventItem(
            UUID eventId,
            String name,
            String category,
            LocalDateTime dateTime,
            String location,
            String rsvpStatus) {}
}
