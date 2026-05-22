package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.application.dto.response.InteractionAnalyticsResponse;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.ports.in.GetInteractionAnalyticsUseCase;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import edu.eci.patriciaM12.infrastructure.external.GeolocationFeignClient;
import edu.eci.patriciaM12.infrastructure.external.dto.ZoneHeatmapResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Application service that implements {@link GetInteractionAnalyticsUseCase} (RF-39).
 * <p>
 * Aggregates total interaction counts, most-active campus zone, peak activity day, and a
 * type-breakdown map for the authenticated student.
 * Applies the <em>Empty Object Pattern</em>: when no persisted metrics exist the response
 * contains zero counts rather than a 404 error.
 * </p>
 *
 * <p><strong>Integration note:</strong> peak zone and interaction type breakdown require
 * event data fed from M06 (Feed & Search) via Kafka topic {@code m12-analytics-group}.
 * Until that consumer is wired in, these fields default to {@code null} / empty.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionAnalyticsService implements GetInteractionAnalyticsUseCase {

    private final StudentMetricsRepositoryPort studentMetricsRepository;
    private final GeolocationFeignClient geolocationFeignClient;

    /**
     * {@inheritDoc}
     * <p>
     * Fetches the student's persisted metrics and derives interaction analytics.
     * The {@code interactionSummary} map is keyed by interaction type name
     * ({@code "parcheAttendance"}, {@code "rsvpConfirmed"}, {@code "connectionRequest"}).
     * </p>
     *
     * @param userId the UUID of the authenticated student
     * @return the computed {@link InteractionAnalyticsResponse}; never {@code null}
     */
    @Override
    public InteractionAnalyticsResponse execute(UUID userId) {
        StudentDashboardMetric metric = studentMetricsRepository.findByUserId(userId).orElse(null);

        int parcheAttendance = metric != null ? metric.getPatchesAttended() : 0;
        int rsvpConfirmed = metric != null && metric.getWeeklyActivity() != null
                ? metric.getWeeklyActivity().values().stream().mapToInt(Integer::intValue).sum()
                : 0;

        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("parcheAttendance", parcheAttendance);
        summary.put("rsvpConfirmed", rsvpConfirmed);
        summary.put("connectionRequest", 0);

        int total = summary.values().stream().mapToInt(Integer::intValue).sum();
        DayOfWeek peakDay = metric != null ? peakDay(metric) : null;

        return InteractionAnalyticsResponse.builder()
                .totalInteractions(total)
                .mostActiveZone(resolveMostActiveZone())
                .peakActivityDay(peakDay)
                .interactionSummary(summary)
                .build();
    }

    /**
     * Resolves the most active campus zone for the current week via the geolocation service.
     * Returns {@code null} on any error (fail-open).
     *
     * @return the {@link CampusZone} with the highest active-user count this week, or {@code null}
     */
    private CampusZone resolveMostActiveZone() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.with(DayOfWeek.MONDAY);
            ZoneHeatmapResponse heatmap = geolocationFeignClient.getCampusHeatmap(
                    weekStart.toString(), today.toString());
            if (heatmap == null || heatmap.zones() == null || heatmap.zones().isEmpty()) {
                return null;
            }
            return heatmap.zones().stream()
                    .max(Comparator.comparingInt(ZoneHeatmapResponse.ZoneEntry::activeUsers))
                    .map(entry -> {
                        try {
                            return CampusZone.valueOf(entry.campusZone().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .orElse(null);
        } catch (Exception e) {
            log.debug("Geolocation unavailable — mostActiveZone will be null: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Determines the day of the week with the highest activity count from the student's
     * weekly activity map.
     *
     * @param metric the student's dashboard metric record
     * @return the {@link DayOfWeek} with the highest activity count, or {@code null} if the map is empty
     */
    private DayOfWeek peakDay(StudentDashboardMetric metric) {
        if (metric.getWeeklyActivity() == null || metric.getWeeklyActivity().isEmpty()) {
            return null;
        }
        return metric.getWeeklyActivity().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
