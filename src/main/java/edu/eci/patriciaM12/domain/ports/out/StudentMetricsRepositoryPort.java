package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;

import java.util.Optional;
import java.util.UUID;

public interface StudentMetricsRepositoryPort {
    Optional<StudentDashboardMetric> findByUserId(UUID userId);
    StudentDashboardMetric save(StudentDashboardMetric metric);
}