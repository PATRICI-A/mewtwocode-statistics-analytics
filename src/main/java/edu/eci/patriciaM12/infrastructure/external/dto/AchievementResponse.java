package edu.eci.patriciaM12.infrastructure.external.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO received from GamificationService's internal endpoint
 * {@code GET /api/v1/gamificacion/internal/user/{userId}/achievements}.
 * Fields mirror {@code EarnedBadgeResponse} in the gamification domain.
 */
public record AchievementResponse(
        UUID badgeId,
        String badgeName,
        LocalDateTime earnedAt,
        int xpAwarded
) {}
