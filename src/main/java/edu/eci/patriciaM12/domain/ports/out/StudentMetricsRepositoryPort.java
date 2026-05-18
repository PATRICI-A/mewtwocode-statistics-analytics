package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port (secondary port) that abstracts persistence operations for
 * {@link StudentDashboardMetric} records (RF-38, RF-39).  Infrastructure adapters (e.g., a JPA
 * repository) must implement this interface so the domain can store and retrieve
 * per-student analytics data without depending on any specific technology.
 */
public interface StudentMetricsRepositoryPort {

    /**
     * Looks up the dashboard metric record for a specific student.
     *
     * @param userId the unique identifier of the student
     * @return an {@link Optional} containing the metric record if one exists, or empty if not
     */
    Optional<StudentDashboardMetric> findByUserId(UUID userId);

    /**
     * Looks up the dashboard metric record for a student within a specific week.
     * The week is identified by any date that falls within it; the adapter resolves the
     * ISO week boundaries (Monday–Sunday) internally.
     *
     * @param userId    the unique identifier of the student
     * @param weekStart the Monday of the target week (inclusive)
     * @param weekEnd   the Sunday of the target week (inclusive)
     * @return an {@link Optional} containing the metric record if one exists, or empty if not
     */
    Optional<StudentDashboardMetric> findByUserIdAndWeek(UUID userId, LocalDate weekStart, LocalDate weekEnd);

    /**
     * Looks up the dashboard metric recorded for a student during the previous calendar week.
     * Used by the social indicators service to compute network growth rate (RF-38 RN-38.3).
     *
     * @param userId    the unique identifier of the student
     * @param weekStart Monday of the previous week
     * @param weekEnd   Sunday of the previous week
     * @return an {@link Optional} containing the prior-week metric if one exists, or empty if not
     */
    Optional<StudentDashboardMetric> findByUserIdAndPreviousWeek(UUID userId, LocalDate weekStart, LocalDate weekEnd);

    /**
     * Persists a new or updated {@link StudentDashboardMetric}.
     *
     * @param metric the metric record to save; must not be {@code null}
     * @return the saved metric record, potentially enriched with infrastructure-assigned metadata
     */
    StudentDashboardMetric save(StudentDashboardMetric metric);
}
