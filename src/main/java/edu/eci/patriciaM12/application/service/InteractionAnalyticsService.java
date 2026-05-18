package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.application.dto.response.InteractionAnalyticsResponse;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.ports.in.GetInteractionAnalyticsUseCase;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
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
@Service
@RequiredArgsConstructor
public class InteractionAnalyticsService implements GetInteractionAnalyticsUseCase {

    private final StudentMetricsRepositoryPort studentMetricsRepository;

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
                .mostActiveZone(null)
                .peakActivityDay(peakDay)
                .interactionSummary(summary)
                .build();
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
