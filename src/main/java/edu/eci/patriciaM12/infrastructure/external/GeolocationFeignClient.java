package edu.eci.patriciaM12.infrastructure.external;

import edu.eci.patriciaM12.infrastructure.external.dto.ZoneHeatmapResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "geolocation-service",
        url = "${services.geolocation.url}"
)
public interface GeolocationFeignClient {

    @GetMapping("/internal/geolocation/heatmap")
    ZoneHeatmapResponse getCampusHeatmap(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);
}
