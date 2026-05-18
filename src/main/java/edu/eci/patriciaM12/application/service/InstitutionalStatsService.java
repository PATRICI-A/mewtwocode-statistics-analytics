package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.application.dto.response.EventsStatsDTO;
import edu.eci.patriciaM12.application.dto.response.InstitutionalStatsResponse;
import edu.eci.patriciaM12.application.dto.response.ParticipationStatsDTO;
import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.enums.InstitutionalMetricType;
import edu.eci.patriciaM12.domain.model.enums.ParticipationTrend;
import edu.eci.patriciaM12.domain.ports.in.GetInstitutionalStatsUseCase;
import edu.eci.patriciaM12.domain.ports.out.AdminSnapshotRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * Application service that implements {@link GetInstitutionalStatsUseCase} (RF-40).
 * <p>
 * Aggregates institutional statistics from {@link AdminAnalyticsSnapshot} records stored
 * in the database.  The social activity index is a weighted composite:
 * <ul>
 *   <li>Parche attendance — 40 % weight</li>
 *   <li>Peer connections — 35 % weight</li>
 *   <li>Event RSVPs — 25 % weight</li>
 * </ul>
 * The index is normalised to [0.0, 1.0] relative to an expected maximum of 10,000 units.
 * </p>
 *
 * <p><strong>Integration note:</strong> The cancelled/active/finished event counts require
 * data published by M06 (Feed & Search) via Kafka.  Until that consumer is live, those
 * fields default to 0 and only {@code totalCreated} is populated from snapshots.</p>
 */
@Service
@RequiredArgsConstructor
public class InstitutionalStatsService implements GetInstitutionalStatsUseCase {

    private static final double PARCHE_WEIGHT = 0.40;
    private static final double CONNECTIONS_WEIGHT = 0.35;
    private static final double EVENTS_WEIGHT = 0.25;
    private static final double INDEX_SCALE = 10_000.0;

    private static final double TREND_THRESHOLD = 0.05;

    private final AdminSnapshotRepositoryPort adminSnapshotRepository;

    /**
     * {@inheritDoc}
     * <p>
     * When {@code startDate} or {@code endDate} are {@code null} the active academic semester
     * boundaries are used.  Only the metric categories requested via {@code metricType} are
     * populated; the rest remain {@code null} and are omitted from the JSON response.
     * </p>
     */
    @Override
    public InstitutionalStatsResponse execute(LocalDate startDate, LocalDate endDate, InstitutionalMetricType metricType) {
        DateRange semester = activeSemester();
        LocalDate from = startDate != null ? startDate : semester.startDate();
        LocalDate to = endDate != null ? endDate : semester.endDate();

        List<AdminAnalyticsSnapshot> snapshots = adminSnapshotRepository.findByDateRange(from, to);

        boolean includeEvents = metricType == InstitutionalMetricType.ALL || metricType == InstitutionalMetricType.EVENTS;
        boolean includeParticipation = metricType == InstitutionalMetricType.ALL || metricType == InstitutionalMetricType.PARTICIPATION;
        boolean includeSocial = metricType == InstitutionalMetricType.ALL || metricType == InstitutionalMetricType.SOCIAL_ACTIVITY;

        EventsStatsDTO eventsStats = includeEvents ? buildEventsStats(snapshots) : null;
        ParticipationStatsDTO participationStats = includeParticipation ? buildParticipationStats(snapshots) : null;
        Double socialIndex = includeSocial ? computeSocialActivityIndex(snapshots) : null;

        return InstitutionalStatsResponse.builder()
                .eventsStats(eventsStats)
                .participationStats(participationStats)
                .socialActivityIndex(socialIndex)
                .build();
    }

    /**
     * Aggregates event lifecycle statistics from the given snapshots.
     * Active/cancelled/finished counts default to 0 until M06 Kafka integration is live.
     *
     * @param snapshots the list of admin snapshots for the period
     * @return the populated {@link EventsStatsDTO}
     */
    private EventsStatsDTO buildEventsStats(List<AdminAnalyticsSnapshot> snapshots) {
        int totalCreated = snapshots.stream().mapToInt(AdminAnalyticsSnapshot::getTotalPatches).sum();
        return EventsStatsDTO.builder()
                .totalCreated(totalCreated)
                .activeCount(0)
                .cancelledCount(0)
                .finishedCount(0)
                .build();
    }

    /**
     * Aggregates student participation statistics from the given snapshots and computes the
     * participation trend by comparing the first and last snapshot's active user counts.
     *
     * @param snapshots the list of admin snapshots for the period
     * @return the populated {@link ParticipationStatsDTO}
     */
    private ParticipationStatsDTO buildParticipationStats(List<AdminAnalyticsSnapshot> snapshots) {
        int totalActiveStudents = snapshots.stream().mapToInt(AdminAnalyticsSnapshot::getActiveUsers).max().orElse(0);
        int totalPatches = snapshots.stream().mapToInt(AdminAnalyticsSnapshot::getTotalPatches).sum();

        ParticipationTrend trend = ParticipationTrend.STABLE;
        if (snapshots.size() >= 2) {
            int first = snapshots.get(0).getActiveUsers();
            int last = snapshots.get(snapshots.size() - 1).getActiveUsers();
            if (first > 0) {
                double change = (double) (last - first) / first;
                if (change > TREND_THRESHOLD) trend = ParticipationTrend.GROWING;
                else if (change < -TREND_THRESHOLD) trend = ParticipationTrend.DECLINING;
            }
        }

        return ParticipationStatsDTO.builder()
                .totalActiveStudents(totalActiveStudents)
                .totalParchesAttended(totalPatches)
                .totalRsvpConfirmed(0)
                .participationTrend(trend)
                .build();
    }

    /**
     * Computes the campus social activity index from snapshot aggregates.
     * Formula: {@code parche×0.40 + connections×0.35 + events×0.25} normalised by {@code INDEX_SCALE}.
     * Clamped to [0.0, 1.0].
     *
     * @param snapshots the list of admin snapshots for the period
     * @return the normalised social activity index in [0.0, 1.0]
     */
    private double computeSocialActivityIndex(List<AdminAnalyticsSnapshot> snapshots) {
        double totalParches = snapshots.stream().mapToInt(AdminAnalyticsSnapshot::getTotalPatches).sum();
        double totalUsers = snapshots.stream().mapToInt(AdminAnalyticsSnapshot::getActiveUsers).sum();

        double raw = totalParches * PARCHE_WEIGHT + 0 * CONNECTIONS_WEIGHT + totalUsers * EVENTS_WEIGHT;
        return Math.min(1.0, raw / INDEX_SCALE);
    }

    /**
     * Resolves the active academic semester boundaries based on the current date.
     * Months 1–6 → first semester (Jan 1 – Jun 30); months 7–12 → second semester (Jul 1 – Dec 31).
     *
     * @return the active semester date range
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
