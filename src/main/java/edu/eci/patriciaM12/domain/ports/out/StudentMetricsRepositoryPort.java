package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port (secondary port) that abstracts persistence operations for
 * {@link StudentDashboardMetric} records.  Infrastructure adapters (e.g., a MongoDB or JPA
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
     * Persists a new or updated {@link StudentDashboardMetric}.
     *
     * @param metric the metric record to save; must not be {@code null}
     * @return the saved metric record, potentially enriched with infrastructure-assigned metadata
     */
    StudentDashboardMetric save(StudentDashboardMetric metric);
}