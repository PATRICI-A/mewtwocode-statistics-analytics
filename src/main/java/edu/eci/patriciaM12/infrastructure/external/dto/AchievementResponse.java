package edu.eci.patriciaM12.infrastructure.external.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AchievementResponse(List<Achievement> achievements) {

    public record Achievement(
            UUID achievementId,
            String name,
            String rarity,
            LocalDateTime unlockedAt) {}
}
