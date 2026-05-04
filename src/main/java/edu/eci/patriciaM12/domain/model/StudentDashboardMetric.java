package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class StudentDashboardMetric {

    private UUID userId;
    private LocalDate period;
    private int patchesAttended;
    private PatchCategory topCategory;
    private Map<DayOfWeek, Integer> weeklyActivity;
    private LocalDateTime computedAt;

    public boolean isStale() {
        return computedAt != null &&
                computedAt.isBefore(LocalDateTime.now().minusMinutes(5));
    }
}