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

/**
 * Spring-managed mapper that converts between {@link StudentDashboardMetricEntity} (JPA layer)
 * and {@link StudentDashboardMetric} (domain layer).
 * The {@code weeklyActivity} JSONB column is stored as a {@code Map<String, Integer>} where
 * keys are {@link java.time.DayOfWeek} names.  Jackson is used for JSON serialisation;
 * parse errors are silently ignored, leaving the map empty.
 */
@Component
@RequiredArgsConstructor
public class StudentMetricsMapper {

    private final ObjectMapper objectMapper;
    private static final TypeReference<Map<String, Integer>> MAP_TYPE = new TypeReference<>() {};

    /**
     * Converts a JPA entity into its corresponding domain model.
     * The {@code weeklyActivity} JSON string is deserialised into a {@code Map<DayOfWeek, Integer>}.
     * If parsing fails the weekly activity map will be empty.
     *
     * @param entity the persistence entity to convert
     * @return the equivalent domain {@link StudentDashboardMetric}
     */
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

    /**
     * Converts a domain model into its corresponding JPA entity.
     * The {@code weeklyActivity} map is serialised to a JSON string with {@link java.time.DayOfWeek}
     * names as keys.  If serialisation fails the column defaults to {@code "{}"}.
     *
     * @param domain the domain metric to convert
     * @return the equivalent {@link StudentDashboardMetricEntity} ready for persistence
     */
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