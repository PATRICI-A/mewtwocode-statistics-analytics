package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.AchievementInfo;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.ParticipationLevel;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response payload containing the personal activity metrics shown on a student's dashboard.
 * This object is always returned (never a 404) thanks to the Empty Object Pattern applied in
 * {@code DashboardService}: when no persisted data exists a synthetic zeroed-out instance is produced.
 *
 * <p>PTR17 additions: {@link #achievementsEarned}, {@link #recentAchievements},
 * and {@link #progressToNextLevel} are populated live from GamificationService.</p>
 */
@Value
@Builder
@Schema(
        name = "StudentDashboardResponse",
        description = """
                Personal activity metrics for a student's dashboard. Implements the Empty Object \
                Pattern — always returns HTTP 200 with zeroed-out values when no activity has \
                been recorded yet, never HTTP 404."""
)
public class StudentDashboardResponse {

    @Schema(
            description = "UUID of the authenticated student",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID userId;

    @Schema(
            description = "Total number of parches the student has attended or joined",
            example = "42"
    )
    int patchesAttended;

    @Schema(
            description = "The parche category in which the student has the highest engagement",
            example = "SPORTS"
    )
    PatchCategory topCategory;

    @Schema(
            description = """
                    Map of day-of-week to activity count, showing the student's activity distribution \
                    across the week. Keys are days (e.g., `MONDAY`, `TUESDAY`), values are interaction counts.""",
            example = "{\"MONDAY\": 12, \"WEDNESDAY\": 8, \"FRIDAY\": 15}"
    )
    Map<DayOfWeek, Integer> weeklyActivity;

    @Schema(
            description = """
                    Qualitative classification of the student's overall participation level.
                    Possible values: `LOW`, `MEDIUM`, `HIGH`, `VERY_HIGH`."""
    )
    ParticipationLevel participationLevel;

    @Schema(
            description = "ISO-8601 timestamp of when this dashboard data was computed",
            example = "2025-06-15T14:30:00.000Z"
    )
    LocalDateTime computedAt;

    /** Total number of badges/monas earned by the student (PTR17). */
    int achievementsEarned;

    /** The 3 most-recently earned badges, sorted by earnedAt descending (PTR17). */
    List<AchievementInfo> recentAchievements;

    /**
     * Progress toward the next gamification level, expressed as a value between 0.0 and 1.0.
     * Returns 1.0 when the student is already at the maximum level (PTR17).
     */
    double progressToNextLevel;

    /**
     * Factory method that converts a domain {@link StudentDashboardMetric} into this response DTO.
     *
     * @param metric the domain metric to convert; may represent a synthetic empty metric
     * @return a new {@link StudentDashboardResponse} populated from the given metric
     */
    public static StudentDashboardResponse from(StudentDashboardMetric metric) {
        List<AchievementInfo> allBadges = metric.getEarnedBadges() != null
                ? metric.getEarnedBadges()
                : List.of();

        List<AchievementInfo> recent = allBadges.stream()
                .sorted(Comparator.comparing(
                        AchievementInfo::earnedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .toList();

        return StudentDashboardResponse.builder()
                .userId(metric.getUserId())
                .patchesAttended(metric.getPatchesAttended())
                .topCategory(metric.getTopCategory())
                .weeklyActivity(metric.getWeeklyActivity())
                .participationLevel(metric.getParticipationLevel())
                .computedAt(metric.getComputedAt())
                .achievementsEarned(allBadges.size())
                .recentAchievements(recent)
                .progressToNextLevel(metric.getProgressToNextLevel())
                .build();
    }
}
