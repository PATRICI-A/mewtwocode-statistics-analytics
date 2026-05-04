package edu.eci.patriciaM12.infrastructure.adapters.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

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

    @Column(name = "snapshot_date", nullable = false, unique = true)
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
}