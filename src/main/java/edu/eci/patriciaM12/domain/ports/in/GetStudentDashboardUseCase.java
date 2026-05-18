package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;

import java.util.UUID;

/**
 * Input port (primary port) that defines the use case for retrieving the analytics
 * dashboard data for a specific student.  Implementations are provided by the
 * application layer and invoked from the web adapter handling student-facing endpoints.
 */
public interface GetStudentDashboardUseCase {

    /**
     * Retrieves or computes the aggregated dashboard metric for the given student.
     * If a cached record exists and is not stale, it is returned directly; otherwise
     * the record is recomputed and persisted before being returned.
     *
     * @param userId the unique identifier of the student whose dashboard is requested
     * @return the {@link StudentDashboardMetric} containing the student's activity data
     * @throws edu.eci.patriciaM12.domain.exceptions.MetricNotFoundException if no metric
     *         record can be found or computed for the given user
     */
    StudentDashboardMetric execute(UUID userId);
}