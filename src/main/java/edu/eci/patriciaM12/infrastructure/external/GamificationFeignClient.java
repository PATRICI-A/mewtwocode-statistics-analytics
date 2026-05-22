package edu.eci.patriciaM12.infrastructure.external;

import edu.eci.patriciaM12.infrastructure.external.dto.AchievementResponse;
import edu.eci.patriciaM12.infrastructure.external.dto.GamificationLevelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * Feign client for GamificationService internal endpoints (PTR17).
 * Called without JWT — Kong routes inter-service traffic directly.
 * URL configured via {@code services.gamification.url}.
 */
@FeignClient(
        name = "gamification-service",
        url = "${services.gamification.url}"
)
public interface GamificationFeignClient {

    /**
     * Returns all badges earned by the given user.
     *
     * @param userId the student's UUID
     * @return list of earned badge DTOs
     */
    @GetMapping("/api/v1/gamificacion/internal/user/{userId}/achievements")
    List<AchievementResponse> getUserAchievements(@PathVariable UUID userId);

    /**
     * Returns level and monas data for the given user.
     * Used to compute {@code progressToNextLevel} on the dashboard.
     *
     * @param userId the student's UUID
     * @return level DTO with totalMonas and monasParaSiguienteNivel
     */
    @GetMapping("/api/v1/gamificacion/internal/user/{userId}/level")
    GamificationLevelResponse getUserLevel(@PathVariable UUID userId);
}
