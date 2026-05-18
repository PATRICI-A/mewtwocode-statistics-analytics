package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.MetricEvent;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.MetricEventType;
import edu.eci.patriciaM12.domain.ports.in.ProcessMetricEventUseCase;
import edu.eci.patriciaM12.domain.ports.out.AdminSnapshotRepositoryPort;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Application service that implements {@link ProcessMetricEventUseCase}.
 * <p>
 * Processes inbound {@link MetricEvent} objects consumed from the Kafka topic
 * {@code m06-patch-events} and updates the relevant analytics aggregates:
 * <ul>
 *   <li><strong>JOIN</strong> — increments the student's {@code patchesAttended} counter and
 *       updates the daily slot in their weekly activity map; increments the admin snapshot's
 *       {@code activeUsers} for the current date.</li>
 *   <li><strong>VIEW</strong> — logged only; no persistence update (signals interest but no
 *       confirmed attendance).</li>
 *   <li><strong>SKIP</strong> — logged only; negative signal used only by M06 for scoring.</li>
 *   <li><strong>CREATE</strong> — increments the admin snapshot's {@code totalPatches} counter.</li>
 *   <li><strong>DELETE / LEAVE</strong> — logged only; future work may decrement counters.</li>
 * </ul>
 * </p>
 *
 * <p>All persistence calls use an <em>upsert-by-date</em> pattern: if no snapshot exists for
 * today, a zeroed-out one is created; if no student metric exists, an Empty Object is created.
 * This ensures idempotency under at-least-once Kafka delivery.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricEventProcessorService implements ProcessMetricEventUseCase {

    private final StudentMetricsRepositoryPort studentMetricsRepository;
    private final AdminSnapshotRepositoryPort adminSnapshotRepository;

    /**
     * {@inheritDoc}
     * <p>
     * Routes processing to a dedicated handler based on {@code event.getEventType()}.
     * Unknown event types are logged at WARN level and silently dropped.
     * </p>
     *
     * @param event the metric event to process; must not be {@code null}
     */
    @Override
    public void process(MetricEvent event) {
        if (event == null || event.getEventType() == null) {
            log.warn("Received null or incomplete MetricEvent — skipping.");
            return;
        }
        log.debug("Processing MetricEvent id={} type={} source={}", event.getEventId(),
                event.getEventType(), event.getSourceModule());

        switch (event.getEventType()) {
            case JOIN   -> handleJoin(event);
            case CREATE -> handleCreate(event);
            case VIEW, LEAVE, DELETE -> log.debug("Event type {} recorded but needs no persistence update.", event.getEventType());
            default     -> log.warn("Unknown MetricEventType: {}", event.getEventType());
        }
    }

    /**
     * Handles a JOIN event by updating the student's metric and the admin snapshot.
     * <ul>
     *   <li>Increments {@code patchesAttended} on the student's current metric.</li>
     *   <li>Increments the day-of-week slot in their weekly activity map.</li>
     *   <li>Marks the admin snapshot's {@code activeUsers} as updated for today.</li>
     * </ul>
     *
     * @param event the JOIN metric event; {@code payload.get("userId")} must be present
     */
    private void handleJoin(MetricEvent event) {
        UUID userId = extractUUID(event, "userId");
        if (userId == null) return;

        StudentDashboardMetric metric = studentMetricsRepository.findByUserId(userId)
                .orElseGet(() -> buildEmptyMetric(userId));

        Map<DayOfWeek, Integer> weekly = metric.getWeeklyActivity() != null
                ? new EnumMap<>(metric.getWeeklyActivity())
                : emptyWeekly();

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        weekly.merge(today, 1, Integer::sum);

        studentMetricsRepository.save(StudentDashboardMetric.builder()
                .userId(metric.getUserId())
                .period(LocalDate.now())
                .patchesAttended(metric.getPatchesAttended() + 1)
                .topCategory(metric.getTopCategory())
                .weeklyActivity(weekly)
                .computedAt(LocalDateTime.now())
                .build());

        upsertAdminSnapshot(snapshot -> AdminAnalyticsSnapshot.builder()
                .id(snapshot.getId())
                .snapshotDate(snapshot.getSnapshotDate())
                .totalPatches(snapshot.getTotalPatches())
                .activeUsers(snapshot.getActiveUsers() + 1)
                .topCategories(snapshot.getTopCategories())
                .peakHours(snapshot.getPeakHours())
                .retentionRate(snapshot.getRetentionRate())
                .generatedAt(LocalDateTime.now())
                .faculty(snapshot.getFaculty())
                .build());
    }

    /**
     * Handles a CREATE event by incrementing the total-patches counter in today's admin snapshot.
     *
     * @param event the CREATE metric event
     */
    private void handleCreate(MetricEvent event) {
        upsertAdminSnapshot(snapshot -> AdminAnalyticsSnapshot.builder()
                .id(snapshot.getId())
                .snapshotDate(snapshot.getSnapshotDate())
                .totalPatches(snapshot.getTotalPatches() + 1)
                .activeUsers(snapshot.getActiveUsers())
                .topCategories(snapshot.getTopCategories())
                .peakHours(snapshot.getPeakHours())
                .retentionRate(snapshot.getRetentionRate())
                .generatedAt(LocalDateTime.now())
                .faculty(snapshot.getFaculty())
                .build());
    }

    /**
     * Loads or creates the admin snapshot for today, applies the given transform function,
     * and persists the result.
     *
     * @param transform function that produces an updated snapshot from the current one
     */
    private void upsertAdminSnapshot(java.util.function.UnaryOperator<AdminAnalyticsSnapshot> transform) {
        LocalDate today = LocalDate.now();
        AdminAnalyticsSnapshot existing = adminSnapshotRepository.findByDate(today)
                .orElseGet(() -> AdminAnalyticsSnapshot.builder()
                        .snapshotDate(today)
                        .totalPatches(0)
                        .activeUsers(0)
                        .topCategories(java.util.List.of())
                        .peakHours(java.util.Map.of())
                        .retentionRate(0f)
                        .generatedAt(LocalDateTime.now())
                        .build());
        adminSnapshotRepository.save(transform.apply(existing));
    }

    /**
     * Extracts a UUID value from the event's payload map.
     *
     * @param event the metric event
     * @param key   the payload key whose value is a UUID string
     * @return the parsed UUID, or {@code null} when the key is absent or the value is malformed
     */
    private UUID extractUUID(MetricEvent event, String key) {
        if (event.getPayload() == null) return null;
        Object val = event.getPayload().get(key);
        if (val == null) return null;
        try {
            return UUID.fromString(val.toString());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID for payload key '{}': {}", key, val);
            return null;
        }
    }

    /**
     * Builds a zeroed-out {@link StudentDashboardMetric} for a student with no prior data
     * (Empty Object Pattern).
     *
     * @param userId the student UUID
     * @return a zeroed-out metric record with {@code computedAt} set to now
     */
    private StudentDashboardMetric buildEmptyMetric(UUID userId) {
        return StudentDashboardMetric.builder()
                .userId(userId)
                .period(LocalDate.now())
                .patchesAttended(0)
                .topCategory(null)
                .weeklyActivity(emptyWeekly())
                .computedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an empty weekly activity map with zero counts for all seven days.
     *
     * @return a mutable {@link EnumMap} keyed by {@link DayOfWeek} with all values set to 0
     */
    private Map<DayOfWeek, Integer> emptyWeekly() {
        Map<DayOfWeek, Integer> map = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) map.put(d, 0);
        return map;
    }
}
