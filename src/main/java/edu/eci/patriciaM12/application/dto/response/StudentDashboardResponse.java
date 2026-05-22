package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.AchievementInfo;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.ParticipationLevel;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
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
public class StudentDashboardResponse {

    UUID userId;
    int patchesAttended;
    PatchCategory topCategory;
    Map<DayOfWeek, Integer> weeklyActivity;
    ParticipationLevel participationLevel;
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
