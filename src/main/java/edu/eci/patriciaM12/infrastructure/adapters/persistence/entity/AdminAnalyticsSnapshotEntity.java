package edu.eci.patriciaM12.infrastructure.adapters.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity that maps to the {@code admin_analytics_snapshot} table.
 * Each row represents a daily snapshot of platform-wide metrics computed for administrators,
 * including active-user counts, total patches, top categories, retention rate, and optional
 * faculty-level segmentation (RF-18 RN-18.10).
 */
@Entity
@Table(name = "admin_analytics_snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAnalyticsSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;


    @Column(name = "total_patches", nullable = false)
    private int totalPatches;

    @Column(name = "active_users", nullable = false)
    private int activeUsers;

    @Column(name = "top_categories", columnDefinition = "TEXT")
    private String topCategories;

    @Column(name = "retention_rate", nullable = false)
    private float retentionRate;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    /**
     * Optional faculty name used to segment snapshots by academic faculty (RF-18 RN-18.10).
     * {@code null} indicates a platform-wide (all-faculties) snapshot.
     */
    @Column(name = "faculty")
    private String faculty;
}
