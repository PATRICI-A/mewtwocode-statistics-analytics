package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.domain.model.AchievementInfo;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.ports.in.GetStudentDashboardUseCase;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import edu.eci.patriciaM12.infrastructure.external.GamificationFeignClient;
import edu.eci.patriciaM12.infrastructure.external.HangoutFeignClient;
import edu.eci.patriciaM12.infrastructure.external.dto.AchievementResponse;
import edu.eci.patriciaM12.infrastructure.external.dto.GamificationLevelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application service that implements the {@link GetStudentDashboardUseCase} use case.
 *
 * <p>Applies the <em>Empty Object Pattern</em>: when no persisted metrics are found for a student,
 * this service returns a synthetic zeroed-out {@link StudentDashboardMetric} instead of throwing
 * an exception or returning {@code null}, ensuring the endpoint always responds with HTTP 200.</p>
 *
 * <p>PTR17: fetches live data from HangoutService (parche count) and GamificationService
 * (earned badges, level progress). Feign failures are swallowed and return safe defaults.</p>
 */
@Service
@RequiredArgsConstructor
public class DashboardService implements GetStudentDashboardUseCase {

    private final StudentMetricsRepositoryPort studentMetricsRepository;
    private final HangoutFeignClient hangoutFeignClient;
    private final GamificationFeignClient gamificationFeignClient;

    /**
     * Retrieves the dashboard metrics for the given student.
     * If no data is found, a synthetic empty metric is returned (Empty Object Pattern).
     *
     * @param userId the UUID of the student whose dashboard metrics are requested
     * @return the persisted {@link StudentDashboardMetric} enriched with live data,
     *         or a zeroed-out synthetic instance when no record exists
     */
    @Override
    public StudentDashboardMetric execute(UUID userId) {
        StudentDashboardMetric base = studentMetricsRepository.findByUserId(userId)
                .orElseGet(() -> buildEmptyMetric(userId));

        int liveParcheCount     = fetchParcheCount(userId);
        List<AchievementInfo> badges = fetchBadges(userId);
        double progress         = fetchProgress(userId);

        return StudentDashboardMetric.builder()
                .userId(base.getUserId())
                .period(base.getPeriod())
                .patchesAttended(liveParcheCount)
                .topCategory(base.getTopCategory())
                .weeklyActivity(base.getWeeklyActivity())
                .computedAt(base.getComputedAt())
                .earnedBadges(badges)
                .progressToNextLevel(progress)
                .build();
    }

    // ── Live data helpers ─────────────────────────────────────────────────────

    private int fetchParcheCount(UUID userId) {
        try {
            Integer count = hangoutFeignClient.getUserParcheCount(userId);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Fetches earned badges from GamificationService and converts them to domain records.
     * Returns an empty list on any Feign error so the dashboard still renders.
     */
    private List<AchievementInfo> fetchBadges(UUID userId) {
        try {
            List<AchievementResponse> responses = gamificationFeignClient.getUserAchievements(userId);
            if (responses == null) return List.of();
            return responses.stream()
                    .map(a -> new AchievementInfo(a.badgeId(), a.badgeName(), a.earnedAt(), a.xpAwarded()))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Computes the fraction of monas collected toward the next level (0.0–1.0).
     * Formula: totalMonas / (totalMonas + monasParaSiguienteNivel).
     * Returns 1.0 when the user is at max level (monasParaSiguienteNivel == 0).
     * Returns 0.0 on any Feign error.
     */
    private double fetchProgress(UUID userId) {
        try {
            GamificationLevelResponse level = gamificationFeignClient.getUserLevel(userId);
            if (level == null) return 0.0;
            if (level.monasParaSiguienteNivel() == 0) return 1.0;
            int denominator = level.totalMonas() + level.monasParaSiguienteNivel();
            return denominator == 0 ? 0.0 : level.totalMonas() / (double) denominator;
        } catch (Exception e) {
            return 0.0;
        }
    }

    // ── Empty Object Pattern ──────────────────────────────────────────────────

    /**
     * Constructs a synthetic empty {@link StudentDashboardMetric} for the given student.
     * All numeric counters are set to zero, the top category is {@code null}, and every
     * day of the week is initialised with a count of zero.
     *
     * @param userId the UUID of the student for whom the empty metric is created
     * @return a zeroed-out {@link StudentDashboardMetric} with {@code computedAt} set to now
     */
    private StudentDashboardMetric buildEmptyMetric(UUID userId) {
        Map<DayOfWeek, Integer> emptyWeekly = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) emptyWeekly.put(day, 0);

        return StudentDashboardMetric.builder()
                .userId(userId)
                .period(LocalDate.now())
                .patchesAttended(0)
                .topCategory(null)
                .weeklyActivity(emptyWeekly)
                .computedAt(LocalDateTime.now())
                .earnedBadges(List.of())
                .progressToNextLevel(0.0)
                .build();
    }
}
