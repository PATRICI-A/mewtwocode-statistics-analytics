package edu.eci.patriciaM12.infrastructure.external.dto;

/**
 * DTO received from GamificationService's internal endpoint
 * {@code GET /api/v1/gamificacion/internal/user/{userId}/level}.
 * Fields mirror {@code UserLevelResponse} in the gamification domain.
 */
public record GamificationLevelResponse(
        String userId,
        int nivel,
        String levelName,
        int totalMonas,
        int monasParaSiguienteNivel
) {}
