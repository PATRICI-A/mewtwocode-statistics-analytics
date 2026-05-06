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

@Value
@Builder
public class StudentDashboardResponse {

    UUID userId;
    int patchesAttended;
    PatchCategory topCategory;
    Map<DayOfWeek, Integer> weeklyActivity;
    ParticipationLevel participationLevel;
    LocalDateTime computedAt;

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