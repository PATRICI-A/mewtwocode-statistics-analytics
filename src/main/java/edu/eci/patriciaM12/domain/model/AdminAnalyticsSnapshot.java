package edu.eci.patriciaM12.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;

/**
 * Immutable aggregate snapshot of platform-wide analytics data intended for
 * administrator consumption.  Each instance represents the state of key metrics
 * (active users, patch counts, category distribution, peak usage hours, and
 * retention) captured on a specific calendar date.
 *
 * <p>Instances are created via the Lombok-generated builder and persisted through
 * {@link edu.eci.patriciaM12.domain.ports.out.AdminSnapshotRepositoryPort}.</p>
 */
@Getter
@Builder
public class AdminAnalyticsSnapshot {

    /** Unique identifier for this snapshot record. */
    private UUID id;

    /** Calendar date for which the snapshot data was collected. */
    private LocalDate snapshotDate;

    /** Total number of patches (activities) existing on the platform on the snapshot date. */
    private int totalPatches;

    /** Number of users who were active on the platform on the snapshot date. */
    private int activeUsers;

    /** Ordered list of category statistics, ranked by activity count descending. */
    private List<CategoryStat> topCategories;

    /**
     * Distribution of platform activity by hour of day.
     * Key: hour of day (0–23); value: number of events recorded in that hour.
     */
    private Map<Integer, Integer> peakHours;

    /** Percentage of users (0.0–1.0) who returned to the platform after their first session. */
    private float retentionRate;

    /** Timestamp at which this snapshot was computed and stored. */
    private LocalDateTime generatedAt;
}