package edu.eci.patriciaM12.infrastructure.adapters.persistence.entity;

import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_dashboard_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDashboardMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "period", nullable = false)
    private LocalDate period;

    @Column(name = "patches_attended", nullable = false)
    private int patchesAttended;

    @Enumerated(EnumType.STRING)
    @Column(name = "top_category")
    private PatchCategory topCategory;

    @Column(name = "weekly_activity", columnDefinition = "jsonb")
    private String weeklyActivity;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;
}