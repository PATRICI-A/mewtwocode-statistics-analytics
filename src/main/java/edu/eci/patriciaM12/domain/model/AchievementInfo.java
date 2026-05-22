package edu.eci.patriciaM12.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight domain record representing a single earned achievement/badge.
 * Populated from GamificationService via inter-service Feign call (PTR17).
 */
public record AchievementInfo(
        UUID badgeId,
        String badgeName,
        LocalDateTime earnedAt,
        int xpAwarded
) {}
