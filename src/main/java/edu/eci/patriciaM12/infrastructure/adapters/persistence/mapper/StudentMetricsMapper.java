package edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.StudentDashboardMetricEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StudentMetricsMapper {

    private final ObjectMapper objectMapper;
    private static final TypeReference<Map<String, Integer>> MAP_TYPE = new TypeReference<>() {};

    public StudentDashboardMetric toDomain(StudentDashboardMetricEntity entity) {
        Map<DayOfWeek, Integer> weekly = new HashMap<>();
        try {
            if (entity.getWeeklyActivity() != null) {
                Map<String, Integer> raw = objectMapper.readValue(entity.getWeeklyActivity(), MAP_TYPE);
                raw.forEach((k, v) -> weekly.put(DayOfWeek.valueOf(k), v));
            }
        } catch (Exception ignored) {}

        return StudentDashboardMetric.builder()
                .userId(entity.getUserId())
                .period(entity.getPeriod())
                .patchesAttended(entity.getPatchesAttended())
                .topCategory(entity.getTopCategory())
                .weeklyActivity(weekly)
                .computedAt(entity.getComputedAt())
                .build();
    }

    public StudentDashboardMetricEntity toEntity(StudentDashboardMetric domain) {
        String weeklyJson = "{}";
        try {
            Map<String, Integer> raw = new HashMap<>();
            domain.getWeeklyActivity().forEach((k, v) -> raw.put(k.name(), v));
            weeklyJson = objectMapper.writeValueAsString(raw);
        } catch (Exception ignored) {}

        return StudentDashboardMetricEntity.builder()
                .userId(domain.getUserId())
                .period(domain.getPeriod())
                .patchesAttended(domain.getPatchesAttended())
                .topCategory(domain.getTopCategory())
                .weeklyActivity(weeklyJson)
                .computedAt(domain.getComputedAt())
                .build();
    }
}