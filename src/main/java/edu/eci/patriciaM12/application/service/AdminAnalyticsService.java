package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
import edu.eci.patriciaM12.application.dto.response.AlertDTO;
import edu.eci.patriciaM12.application.dto.response.AnalyticsDTO;
import edu.eci.patriciaM12.application.dto.response.EventAnalyticsDTO;
import edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException;
import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.enums.MetricType;
import edu.eci.patriciaM12.domain.ports.in.GetAdminAnalyticsUseCase;
import edu.eci.patriciaM12.domain.ports.out.AdminSnapshotRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application service that implements the {@link GetAdminAnalyticsUseCase} use case (RF-18).
 * <p>
 * Applies the <em>Semester Range Strategy</em>: when no explicit date range is supplied the
 * service automatically resolves the active academic semester.  Months 1–6 map to the
 * first semester (January 1 – June 30) and months 7–12 map to the second semester
 * (July 1 – December 31) of the current year.
 * </p>
 *
 * <p>Alert generation (RN-18.6): any metric that drops more than 30 % compared to the
 * previous week triggers an {@link AlertDTO} included in the response.
 * </p>
 *
 * <p><strong>Integration note:</strong> {@code topEvents} and {@code campusHeatmap} require
 * event data published by M06 (Feed & Search) via Kafka topic {@code m12-analytics-group}.
 * Until that consumer is live the fields return safe defaults (empty list / null).
 * {@code matchSuccessRate} requires data from M05 (Notifications/Matches); defaults to 0.0
 * until that integration is live.</p>
 */
@Service
@RequiredArgsConstructor
public class AdminAnalyticsService implements GetAdminAnalyticsUseCase {

    private static final double ALERT_DROP_THRESHOLD = 0.30;

    private final AdminSnapshotRepositoryPort adminSnapshotRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public AdminAnalyticsSnapshot getLatest(LocalDate date) {
        return adminSnapshotRepository.findByDate(date).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdminAnalyticsSnapshot> getHistorical(LocalDate dateFrom, LocalDate dateTo) {
        return adminSnapshotRepository.findByDateRange(dateFrom, dateTo);
    }

    /**
     * Builds the admin analytics panel response for the requested date range, metric type,
     * and optional faculty filter (RF-18 RN-18.10).
     * <p>
     * If {@code startDate} or {@code endDate} are {@code null}, the boundaries of the active
     * academic semester are used instead.  Only the metrics that match {@code metricType} are
     * populated; passing {@code null} includes all metrics.  When {@code facultyFilter} is
     * non-blank, only snapshots tagged with that faculty are considered.
     * </p>
     *
     * @param startDate     the start of the query window, or {@code null} for semester start
     * @param endDate       the end of the query window, or {@code null} for semester end
     * @param metricType    the specific metric to include, or {@code null} to include all
     * @param facultyFilter optional faculty name restriction; {@code null} means all faculties
     * @return a fully built {@link AdminAnalyticsResponse} with the requested data
     * @throws InvalidReportFiltersException if the resolved date range is invalid
     */
    @Override
    public AdminAnalyticsResponse getPanel(LocalDate startDate, LocalDate endDate,
                                           MetricType metricType, String facultyFilter) {
        DateRange activeSemester = activeSemester();
        LocalDate resolvedStart = startDate == null ? activeSemester.startDate() : startDate;
        LocalDate resolvedEnd = endDate == null ? activeSemester.endDate() : endDate;
        validateDateRange(startDate, resolvedStart, resolvedEnd, activeSemester);

        List<AdminAnalyticsSnapshot> snapshots = (facultyFilter != null && !facultyFilter.isBlank())
                ? adminSnapshotRepository.findByDateRangeAndFaculty(resolvedStart, resolvedEnd, facultyFilter)
                : adminSnapshotRepository.findByDateRange(resolvedStart, resolvedEnd);

        List<AdminAnalyticsSnapshot> previousWeekSnapshots = adminSnapshotRepository
                .findByDateRange(resolvedStart.minusWeeks(1), resolvedEnd.minusWeeks(1));

        List<AlertDTO> alerts = include(metricType, MetricType.USERS)
                ? buildAlerts(snapshots, previousWeekSnapshots)
                : List.of();

        return AdminAnalyticsResponse.builder()
                .activeUsers(include(metricType, MetricType.USERS) ? buildActiveUsers(snapshots) : null)
                .parcheStats(include(metricType, MetricType.PARCHES) ? buildParcheStats(snapshots) : null)
                .topEvents(include(metricType, MetricType.EVENTS) ? List.<EventAnalyticsDTO>of() : null)
                .matchSuccessRate(include(metricType, MetricType.MATCHES) ? 0.0 : null)
                .campusHeatmap(null)
                .retentionRate(include(metricType, MetricType.USERS) ? computeRetentionRate(snapshots, previousWeekSnapshots) : null)
                .abandonedParches(include(metricType, MetricType.PARCHES) ? 0 : null)
                .avgTimeToFirstMember(include(metricType, MetricType.PARCHES) ? 0L : null)
                .alerts(alerts.isEmpty() ? null : alerts)
                .build();
    }

    /**
     * Determines whether a given candidate metric should be included in the response.
     *
     * @param requested the metric type requested by the caller; may be {@code null}
     * @param candidate the metric type being evaluated for inclusion
     * @return {@code true} if the candidate metric should be included
     */
    private boolean include(MetricType requested, MetricType candidate) {
        return requested == null || requested == candidate;
    }

    /**
     * Constructs the active-users {@link AnalyticsDTO} as a chronological time-series.
     *
     * @param snapshots the list of snapshots to aggregate
     * @return an {@link AnalyticsDTO} containing the time-series of active users
     */
    private AnalyticsDTO buildActiveUsers(List<AdminAnalyticsSnapshot> snapshots) {
        Map<LocalDate, Integer> timeSeries = new LinkedHashMap<>();
        snapshots.forEach(s -> timeSeries.put(s.getSnapshotDate(), s.getActiveUsers()));
        return AnalyticsDTO.builder().timeSeries(timeSeries).build();
    }

    /**
     * Constructs the parche-statistics {@link AnalyticsDTO} by summing total patches.
     *
     * @param snapshots the list of snapshots to aggregate
     * @return an {@link AnalyticsDTO} with the aggregated total patch count
     */
    private AnalyticsDTO buildParcheStats(List<AdminAnalyticsSnapshot> snapshots) {
        int total = snapshots.stream().mapToInt(AdminAnalyticsSnapshot::getTotalPatches).sum();
        return AnalyticsDTO.builder().total(total).categories(List.of()).build();
    }

    /**
     * Computes retention rate as the ratio of users who were active in both the current period
     * and the previous equivalent period.
     * <p>
     * Returns 0.0 when no previous snapshots are available.
     * </p>
     *
     * @param current  snapshots in the current period
     * @param previous snapshots in the previous equivalent period
     * @return retention rate as a percentage [0.0 – 100.0]
     */
    private Double computeRetentionRate(List<AdminAnalyticsSnapshot> current, List<AdminAnalyticsSnapshot> previous) {
        if (previous.isEmpty() || current.isEmpty()) return 0.0;
        double avgPrevious = previous.stream().mapToInt(AdminAnalyticsSnapshot::getActiveUsers).average().orElse(0.0);
        double avgCurrent = current.stream().mapToInt(AdminAnalyticsSnapshot::getActiveUsers).average().orElse(0.0);
        if (avgPrevious == 0.0) return 0.0;
        return Math.min(100.0, avgCurrent / avgPrevious * 100.0);
    }

    /**
     * Generates alert DTOs for any metric that dropped more than 30 % relative to the previous week
     * (RF-18 RN-18.6).
     *
     * @param current  snapshots in the current period
     * @param previous snapshots in the previous equivalent period
     * @return list of alerts; empty when no threshold is exceeded
     */
    private List<AlertDTO> buildAlerts(List<AdminAnalyticsSnapshot> current, List<AdminAnalyticsSnapshot> previous) {
        List<AlertDTO> alerts = new ArrayList<>();

        double prevAvgUsers = previous.stream().mapToInt(AdminAnalyticsSnapshot::getActiveUsers).average().orElse(0.0);
        double currAvgUsers = current.stream().mapToInt(AdminAnalyticsSnapshot::getActiveUsers).average().orElse(0.0);
        checkAlert("activeUsers", prevAvgUsers, currAvgUsers, alerts);

        double prevTotalPatches = previous.stream().mapToInt(AdminAnalyticsSnapshot::getTotalPatches).sum();
        double currTotalPatches = current.stream().mapToInt(AdminAnalyticsSnapshot::getTotalPatches).sum();
        checkAlert("totalPatches", prevTotalPatches, currTotalPatches, alerts);

        return alerts;
    }

    /**
     * Checks whether a metric has dropped beyond the 30 % threshold and adds an alert if so.
     *
     * @param metricName the name of the metric being checked
     * @param previous   the previous period value
     * @param current    the current period value
     * @param alerts     the list to append the alert to when the threshold is exceeded
     */
    private void checkAlert(String metricName, double previous, double current, List<AlertDTO> alerts) {
        if (previous <= 0) return;
        double drop = (previous - current) / previous;
        if (drop > ALERT_DROP_THRESHOLD) {
            double dropPercent = drop * 100.0;
            alerts.add(AlertDTO.builder()
                    .metricName(metricName)
                    .dropPercentage(dropPercent)
                    .message(String.format("Metric '%s' dropped %.1f%% compared to the previous week.", metricName, dropPercent))
                    .build());
        }
    }

    /**
     * Validates that the resolved date range is internally consistent and within the active semester.
     *
     * @param startDate         the raw start date supplied by the caller (may be {@code null})
     * @param resolvedStartDate the effective start date after applying the semester default
     * @param resolvedEndDate   the effective end date after applying the semester default
     * @param activeSemester    the currently active semester boundaries
     * @throws InvalidReportFiltersException if validation fails
     */
    private void validateDateRange(LocalDate startDate, LocalDate resolvedStartDate, LocalDate resolvedEndDate,
                                   DateRange activeSemester) {
        if (startDate != null
                && (startDate.isBefore(activeSemester.startDate()) || startDate.isAfter(activeSemester.endDate()))) {
            throw new InvalidReportFiltersException("startDate must be within the active semester.");
        }
        if (!resolvedEndDate.isAfter(resolvedStartDate)) {
            throw new InvalidReportFiltersException("endDate must be after startDate.");
        }
    }

    /**
     * Resolves the active academic semester based on the current date.
     *
     * @return a {@link DateRange} representing the active semester boundaries
     */
    private DateRange activeSemester() {
        LocalDate today = LocalDate.now();
        if (today.getMonthValue() <= Month.JUNE.getValue()) {
            return new DateRange(LocalDate.of(today.getYear(), Month.JANUARY, 1),
                    LocalDate.of(today.getYear(), Month.JUNE, 30));
        }
        return new DateRange(LocalDate.of(today.getYear(), Month.JULY, 1),
                LocalDate.of(today.getYear(), Month.DECEMBER, 31));
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {}
}
