package edu.eci.patriciaM12.infrastructure.external;

import edu.eci.patriciaM12.infrastructure.external.dto.EventAgendaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "campus-events-service",
        url = "${services.campus-events.url}"
)
public interface CampusEventsFeignClient {

    @GetMapping("/internal/events/agenda/{userId}")
    EventAgendaResponse getUserEventAgenda(@PathVariable UUID userId);
}
