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

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService implements GetAdminAnalyticsUseCase {

    private final AdminSnapshotRepositoryPort adminSnapshotRepository;

    @Override
    public AdminAnalyticsSnapshot getLatest(LocalDate date) {
        return adminSnapshotRepository.findByDate(date).orElse(null);
    }

    @Override
    public List<AdminAnalyticsSnapshot> getHistorical(LocalDate dateFrom, LocalDate dateTo) {
        return adminSnapshotRepository.findByDateRange(dateFrom, dateTo);
    }

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

    private boolean include(MetricType requested, MetricType candidate) {
        return requested == null || requested == candidate;
    }

    private AnalyticsDTO buildActiveUsers(List<AdminAnalyticsSnapshot> snapshots) {
        Map<LocalDate, Integer> timeSeries = new LinkedHashMap<>();
        snapshots.forEach(snapshot -> timeSeries.put(snapshot.getSnapshotDate(), snapshot.getActiveUsers()));
        return AnalyticsDTO.builder()
                .timeSeries(timeSeries)
                .build();
    }

    private AnalyticsDTO buildParcheStats(List<AdminAnalyticsSnapshot> snapshots) {
        int total = snapshots.stream()
                .mapToInt(AdminAnalyticsSnapshot::getTotalPatches)
                .sum();
        return AnalyticsDTO.builder()
                .total(total)
                .categories(List.of())
                .build();
    }

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

    private DateRange activeSemester() {
        LocalDate today = LocalDate.now();
        if (today.getMonthValue() <= Month.JUNE.getValue()) {
            return new DateRange(LocalDate.of(today.getYear(), Month.JANUARY, 1),
                    LocalDate.of(today.getYear(), Month.JUNE, 30));
        }
        return new DateRange(LocalDate.of(today.getYear(), Month.JULY, 1),
                LocalDate.of(today.getYear(), Month.DECEMBER, 31));
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
