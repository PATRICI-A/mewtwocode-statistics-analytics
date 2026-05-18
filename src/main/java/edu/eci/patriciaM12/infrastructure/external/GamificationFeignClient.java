package edu.eci.patriciaM12.infrastructure.external;

import edu.eci.patriciaM12.infrastructure.external.dto.AchievementResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "gamification-service",
        url = "${services.gamification.url}"
)
public interface GamificationFeignClient {

    @GetMapping("/internal/gamification/{userId}/achievements")
    AchievementResponse getUserAchievements(@PathVariable UUID userId);
}
