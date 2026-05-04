package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;

import java.util.UUID;

public interface GetStudentDashboardUseCase {
    StudentDashboardMetric execute(UUID userId);
}