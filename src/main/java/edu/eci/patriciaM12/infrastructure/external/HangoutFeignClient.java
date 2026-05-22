package edu.eci.patriciaM12.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "hangout-service",
        url = "${services.hangout.url}"
)
public interface HangoutFeignClient {

    @GetMapping("/api/v1/parches/internal/user/{userId}/parche-count")
    Integer getUserParcheCount(@PathVariable UUID userId);
}
