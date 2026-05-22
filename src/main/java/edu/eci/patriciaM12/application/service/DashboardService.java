package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.ports.in.GetStudentDashboardUseCase;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import edu.eci.patriciaM12.infrastructure.external.HangoutFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Application service that implements the {@link GetStudentDashboardUseCase} use case.
 * <p>
 * Applies the <em>Empty Object Pattern</em>: when no persisted metrics are found for a student,
 * this service returns a synthetic zeroed-out {@link StudentDashboardMetric} instead of throwing
 * an exception or returning {@code null}, ensuring the endpoint always responds with HTTP 200.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DashboardService implements GetStudentDashboardUseCase {

    private final StudentMetricsRepositoryPort studentMetricsRepository;
    private final HangoutFeignClient hangoutFeignClient;

    /**
     * Retrieves the dashboard metrics for the given student.
     * If no data is found, a synthetic empty metric is returned (Empty Object Pattern).
     *
     * @param userId the UUID of the student whose dashboard metrics are requested
     * @return the persisted {@link StudentDashboardMetric} if found, or a zeroed-out
     *         synthetic instance when no record exists
     */
    @Override
    public StudentDashboardMetric execute(UUID userId) {
        StudentDashboardMetric base = studentMetricsRepository.findByUserId(userId)
                .orElseGet(() -> buildEmptyMetric(userId));
        int liveParcheCount = fetchParcheCount(userId);
        return StudentDashboardMetric.builder()
                .userId(base.getUserId())
                .period(base.getPeriod())
                .patchesAttended(liveParcheCount)
                .topCategory(base.getTopCategory())
                .weeklyActivity(base.getWeeklyActivity())
                .computedAt(base.getComputedAt())
                .build();
    }

    private int fetchParcheCount(UUID userId) {
        try {
            Integer count = hangoutFeignClient.getUserParcheCount(userId);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * Constructs a synthetic empty {@link StudentDashboardMetric} for the given student.
     * All numeric counters are set to zero, the top category is {@code null}, and every
     * day of the week is initialised with a count of zero.
     *
     * @param userId the UUID of the student for whom the empty metric is created
     * @return a zeroed-out {@link StudentDashboardMetric} with {@code computedAt} set to now
     */
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