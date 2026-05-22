package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.application.dto.response.NetworkGrowthDTO;
import edu.eci.patriciaM12.application.dto.response.SocialAffinityDTO;
import edu.eci.patriciaM12.application.dto.response.SocialIndicatorsResponse;
import edu.eci.patriciaM12.application.dto.response.WeeklyParticipationDTO;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.ActivityLevel;
import edu.eci.patriciaM12.domain.ports.in.GetSocialIndicatorsUseCase;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import edu.eci.patriciaM12.infrastructure.external.CampusEventsFeignClient;
import edu.eci.patriciaM12.infrastructure.external.HangoutFeignClient;
import edu.eci.patriciaM12.infrastructure.external.ProfileFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

/**
 * Application service that implements {@link GetSocialIndicatorsUseCase} (RF-38).
 * <p>
 * Computes weekly participation, network growth, and social affinity for a student.
 * The social affinity score is a weighted composite:
 * <ul>
 *   <li>Shared interests (PatchCategory overlap) — 40 %</li>
 *   <li>Common parches attended — 35 %</li>
 *   <li>Mutual peer connections — 25 %</li>
 * </ul>
 * When no persisted data is found the service returns zeroed-out indicators (Empty Object Pattern).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SocialIndicatorsService implements GetSocialIndicatorsUseCase {

    private static final double INTERESTS_WEIGHT = 0.40;
    private static final double PARCHES_WEIGHT = 0.35;
    private static final double CONNECTIONS_WEIGHT = 0.25;

    private final StudentMetricsRepositoryPort studentMetricsRepository;
    private final HangoutFeignClient hangoutFeignClient;
    private final CampusEventsFeignClient campusEventsFeignClient;
    private final ProfileFeignClient profileFeignClient;

    /**
     * {@inheritDoc}
     * <p>
     * Resolves the ISO week boundaries for the requested week offset, fetches the current and
     * previous week metrics, and computes all social indicator dimensions.
     * </p>
     *
     * @param userId    the UUID of the authenticated student
     * @param weekRange week offset (0 = current week, 1 = last week, …); defaults to 0 when {@code null}
     * @return the fully populated {@link SocialIndicatorsResponse}; never {@code null}
     */
    @Override
    public SocialIndicatorsResponse execute(UUID userId, Integer weekRange) {
        int offset = weekRange != null ? weekRange : 0;

        LocalDate targetMonday = LocalDate.now()
                .minusWeeks(offset)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate targetSunday = targetMonday.plusDays(6);

        LocalDate prevMonday = targetMonday.minusWeeks(1);
        LocalDate prevSunday = targetSunday.minusWeeks(1);

        StudentDashboardMetric current = studentMetricsRepository
                .findByUserIdAndWeek(userId, targetMonday, targetSunday)
                .orElse(null);

        StudentDashboardMetric previous = studentMetricsRepository
                .findByUserIdAndPreviousWeek(userId, prevMonday, prevSunday)
                .orElse(null);

        WeeklyParticipationDTO participation = buildParticipation(current, userId);
        NetworkGrowthDTO networkGrowth = buildNetworkGrowth(current, previous, userId);
        SocialAffinityDTO affinity = buildAffinity(current);
        ActivityLevel level = classifyActivityLevel(participation);

        return SocialIndicatorsResponse.builder()
                .weeklyParticipation(participation)
                .networkGrowth(networkGrowth)
                .socialAffinity(affinity)
                .activityLevel(level)
                .build();
    }

    /**
     * Builds the weekly participation DTO from the student's current-week metric.
     * Returns zeroed counts when no metric exists.
     *
     * @param metric the current-week metric record; may be {@code null}
     * @return the populated {@link WeeklyParticipationDTO}
     */
    private WeeklyParticipationDTO buildParticipation(StudentDashboardMetric metric, UUID userId) {
        int parcheCount = fetchSafe(() -> hangoutFeignClient.getUserParcheCount(userId));
        int eventRsvpCount = fetchSafe(() -> campusEventsFeignClient.getUserRsvpCount(userId));
        int activeConnections = fetchSafe(() -> profileFeignClient.getUserConnectionsCount(userId));
        return WeeklyParticipationDTO.builder()
                .parcheCount(parcheCount)
                .eventRsvpCount(eventRsvpCount)
                .activeConnections(activeConnections)
                .build();
    }

    /**
     * Computes the network growth DTO by comparing current and previous week connection counts.
     * Growth rate is {@code null} when the previous week had zero connections.
     *
     * @param current  current-week metric; may be {@code null}
     * @param previous previous-week metric; may be {@code null}
     * @return the populated {@link NetworkGrowthDTO}
     */
    private NetworkGrowthDTO buildNetworkGrowth(StudentDashboardMetric current, StudentDashboardMetric previous, UUID userId) {
        int currentConnections = fetchSafe(() -> profileFeignClient.getUserConnectionsCount(userId));
        int previousConnections = 0;
        Double growthRate = null;

        if (previousConnections > 0) {
            growthRate = (double) (currentConnections - previousConnections) / previousConnections * 100.0;
        }

        return NetworkGrowthDTO.builder()
                .currentWeekConnections(currentConnections)
                .previousWeekConnections(previousConnections)
                .growthRate(growthRate)
                .build();
    }

    /**
     * Derives social affinity scores from the student's metric record.
     * All component scores are currently approximated from available data; once M05 and M06
     * integration events are wired in via Kafka they will be populated with real values.
     *
     * @param metric the current-week metric; may be {@code null}
     * @return the weighted {@link SocialAffinityDTO}
     */
    private SocialAffinityDTO buildAffinity(StudentDashboardMetric metric) {
        double interestsScore = 0.0;
        double parchesScore = metric != null ? Math.min(1.0, metric.getPatchesAttended() / 10.0) : 0.0;
        double connectionsScore = 0.0;

        double total = interestsScore * INTERESTS_WEIGHT
                + parchesScore * PARCHES_WEIGHT
                + connectionsScore * CONNECTIONS_WEIGHT;

        return SocialAffinityDTO.builder()
                .sharedInterestsScore(interestsScore)
                .commonParchesScore(parchesScore)
                .mutualConnectionsScore(connectionsScore)
                .totalScore(total)
                .build();
    }

    private int fetchSafe(java.util.function.Supplier<Integer> call) {
        try {
            Integer result = call.get();
            return result != null ? result : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private ActivityLevel classifyActivityLevel(WeeklyParticipationDTO participation) {
        int total = participation.getParcheCount() + participation.getEventRsvpCount();
        if (total >= 10) return ActivityLevel.VERY_HIGH;
        if (total >= 5) return ActivityLevel.HIGH;
        if (total >= 2) return ActivityLevel.MEDIUM;
        return ActivityLevel.LOW;
    }
}
