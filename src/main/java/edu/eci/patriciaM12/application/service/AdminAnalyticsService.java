package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application service that implements the {@link GetAdminAnalyticsUseCase} use case.
 * <p>
 * Applies the <em>Semester Range Strategy</em>: when no explicit date range is supplied the
 * service automatically resolves the active academic semester.  Months 1–6 map to the
 * first semester (January 1 – June 30) and months 7–12 map to the second semester
 * (July 1 – December 31) of the current year.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AdminAnalyticsService implements GetAdminAnalyticsUseCase {

    private final AdminSnapshotRepositoryPort adminSnapshotRepository;

    /**
     * Retrieves the admin analytics snapshot recorded on the given date.
     *
     * @param date the exact date for which the snapshot is requested
     * @return the matching {@link AdminAnalyticsSnapshot}, or {@code null} if none exists
     */
    @Override
    public AdminAnalyticsSnapshot getLatest(LocalDate date) {
        return adminSnapshotRepository.findByDate(date).orElse(null);
    }

    /**
     * Retrieves all admin analytics snapshots recorded within the given date range, inclusive.
     *
     * @param dateFrom the start of the range (inclusive)
     * @param dateTo   the end of the range (inclusive)
     * @return an ordered list of {@link AdminAnalyticsSnapshot} objects; never {@code null}
     */
    @Override
    public List<AdminAnalyticsSnapshot> getHistorical(LocalDate dateFrom, LocalDate dateTo) {
        return adminSnapshotRepository.findByDateRange(dateFrom, dateTo);
    }

    /**
     * Builds the admin analytics panel response for the requested date range and metric type.
     * <p>
     * If {@code startDate} or {@code endDate} are {@code null}, the boundaries of the active
     * academic semester are used instead.  Only the metrics that match {@code metricType} are
     * populated; passing {@code null} includes all metrics.
     * </p>
     *
     * @param startDate  the start of the query window, or {@code null} to default to the semester start
     * @param endDate    the end of the query window, or {@code null} to default to the semester end
     * @param metricType the specific metric to include, or {@code null} to include all metrics
     * @return a fully built {@link AdminAnalyticsResponse} with the requested data
     * @throws edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException if the resolved date
     *         range is invalid or falls outside the active semester
     */
    @Override
    public AdminAnalyticsResponse getPanel(LocalDate startDate, LocalDate endDate, MetricType metricType) {
        DateRange activeSemester = activeSemester();
        LocalDate resolvedStartDate = startDate == null ? activeSemester.startDate() : startDate;
        LocalDate resolvedEndDate = endDate == null ? activeSemester.endDate() : endDate;
        validateDateRange(startDate, resolvedStartDate, resolvedEndDate, activeSemester);

        List<AdminAnalyticsSnapshot> snapshots = adminSnapshotRepository
                .findByDateRange(resolvedStartDate, resolvedEndDate);

        return AdminAnalyticsResponse.builder()
                .activeUsers(include(metricType, MetricType.USERS) ? buildActiveUsers(snapshots) : null)
                .parcheStats(include(metricType, MetricType.PARCHES) ? buildParcheStats(snapshots) : null)
                .topEvents(include(metricType, MetricType.EVENTS) ? List.<EventAnalyticsDTO>of() : null)
                .matchSuccessRate(include(metricType, MetricType.MATCHES) ? 0.0 : null)
                .campusHeatmap(null)
                .build();
    }

    /**
     * Determines whether a given candidate metric should be included in the response.
     * Returns {@code true} when {@code requested} is {@code null} (all metrics) or when it
     * matches the {@code candidate} exactly.
     *
     * @param requested the metric type requested by the caller; may be {@code null}
     * @param candidate the metric type being evaluated for inclusion
     * @return {@code true} if the candidate metric should be included; {@code false} otherwise
     */
    private boolean include(MetricType requested, MetricType candidate) {
        return requested == null || requested == candidate;
    }

    /**
     * Constructs the active-users {@link AnalyticsDTO} by mapping each snapshot's date to
     * its active-user count and building a chronological time-series.
     *
     * @param snapshots the list of snapshots to aggregate
     * @return an {@link AnalyticsDTO} containing the time-series of active users
     */
    private AnalyticsDTO buildActiveUsers(List<AdminAnalyticsSnapshot> snapshots) {
        Map<LocalDate, Integer> timeSeries = new LinkedHashMap<>();
        snapshots.forEach(snapshot -> timeSeries.put(snapshot.getSnapshotDate(), snapshot.getActiveUsers()));
        return AnalyticsDTO.builder()
                .timeSeries(timeSeries)
                .build();
    }

    /**
     * Constructs the parche-statistics {@link AnalyticsDTO} by summing the total number of
     * patches across all provided snapshots.
     *
     * @param snapshots the list of snapshots to aggregate
     * @return an {@link AnalyticsDTO} with the aggregated total patch count
     */
    private AnalyticsDTO buildParcheStats(List<AdminAnalyticsSnapshot> snapshots) {
        int total = snapshots.stream()
                .mapToInt(AdminAnalyticsSnapshot::getTotalPatches)
                .sum();
        return AnalyticsDTO.builder()
                .total(total)
                .categories(List.of())
                .build();
    }

    /**
     * Validates that the resolved date range is internally consistent and within the active semester.
     *
     * @param startDate         the raw start date supplied by the caller (may be {@code null})
     * @param resolvedStartDate the effective start date after applying the semester default
     * @param resolvedEndDate   the effective end date after applying the semester default
     * @param activeSemester    the currently active semester boundaries
     * @throws edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException if the start date
     *         falls outside the active semester, or if the end date is not after the start date
     */
    private void validateDateRange(LocalDate startDate, LocalDate resolvedStartDate, LocalDate resolvedEndDate,
                                   DateRange activeSemester) {
        if (startDate != null
                && (startDate.isBefore(activeSemester.startDate()) || startDate.isAfter(activeSemester.endDate()))) {
            throw new InvalidReportFiltersException("startDate debe estar dentro del semestre activo.");
        }
        if (!resolvedEndDate.isAfter(resolvedStartDate)) {
            throw new InvalidReportFiltersException("endDate debe ser posterior a startDate.");
        }
    }

    /**
     * Resolves the active academic semester based on the current date.
     * <p>
     * Months 1–6 (January to June) correspond to the first semester; months 7–12
     * (July to December) correspond to the second semester of the current year.
     * </p>
     *
     * @return a {@link DateRange} record representing the start and end of the active semester
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

    /**
     * Immutable value object that holds the inclusive start and end dates of an academic semester.
     *
     * @param startDate the first day of the semester
     * @param endDate   the last day of the semester
     */
    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
