package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.ParticipationLevel;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import lombok.Builder;
import lombok.Value;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response payload containing the personal activity metrics shown on a student's dashboard.
 * This object is always returned (never a 404) thanks to the Empty Object Pattern applied in
 * {@code DashboardService}: when no persisted data exists a synthetic zeroed-out instance is produced.
 */
@Value
@Builder
public class StudentDashboardResponse {

    UUID userId;
    int patchesAttended;
    PatchCategory topCategory;
    Map<DayOfWeek, Integer> weeklyActivity;
    ParticipationLevel participationLevel;
    LocalDateTime computedAt;

    /**
     * Factory method that converts a domain {@link StudentDashboardMetric} into this response DTO.
     *
     * @param metric the domain metric to convert; may represent a synthetic empty metric
     * @return a new {@link StudentDashboardResponse} populated from the given metric
     */
    public static StudentDashboardResponse from(StudentDashboardMetric metric) {
        return StudentDashboardResponse.builder()
                .userId(metric.getUserId())
                .patchesAttended(metric.getPatchesAttended())
                .topCategory(metric.getTopCategory())
                .weeklyActivity(metric.getWeeklyActivity())
                .participationLevel(metric.getParticipationLevel())
                .computedAt(metric.getComputedAt())
                .build();
    }
}