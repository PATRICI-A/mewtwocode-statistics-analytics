package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.ports.in.GetStudentDashboardUseCase;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService implements GetStudentDashboardUseCase {

    private final StudentMetricsRepositoryPort studentMetricsRepository;

    @Override
    public StudentDashboardMetric execute(UUID userId) {
        return studentMetricsRepository.findByUserId(userId)
                .orElseGet(() -> buildEmptyMetric(userId));
    }


    private StudentDashboardMetric buildEmptyMetric(UUID userId) {
        Map<DayOfWeek, Integer> emptyWeekly = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) emptyWeekly.put(day, 0);

        return StudentDashboardMetric.builder()
                .userId(userId)
                .period(LocalDate.now())
                .patchesAttended(0)
                .topCategory(null)
                .weeklyActivity(emptyWeekly)
                .computedAt(LocalDateTime.now())
                .build();
    }
}