package edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper;

import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.AdminAnalyticsSnapshotEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Spring-managed mapper that converts between {@link AdminAnalyticsSnapshotEntity} (JPA layer)
 * and {@link AdminAnalyticsSnapshot} (domain layer).
 * The {@code topCategories} and {@code peakHours} fields are not yet persisted in the entity
 * and default to empty collections during conversion.
 */
@Component
public class AdminAnalyticsSnapshotMapper {

    /**
     * Converts a JPA entity into its corresponding domain model.
     *
     * @param entity the persistence entity to convert
     * @return the equivalent domain {@link AdminAnalyticsSnapshot}
     */
    public AdminAnalyticsSnapshot toDomain(AdminAnalyticsSnapshotEntity entity) {
        return AdminAnalyticsSnapshot.builder()
                .id(entity.getId())
                .snapshotDate(entity.getSnapshotDate())
                .totalPatches(entity.getTotalPatches())
                .activeUsers(entity.getActiveUsers())
                .topCategories(List.of())
                .peakHours(Map.of())
                .retentionRate(entity.getRetentionRate())
                .generatedAt(entity.getGeneratedAt())
                .build();
    }

    /**
     * Converts a domain model into its corresponding JPA entity.
     * The {@code topCategories} column is serialised as the literal string {@code "[]"}.
     *
     * @param domain the domain snapshot to convert
     * @return the equivalent {@link AdminAnalyticsSnapshotEntity} ready for persistence
     */
    public AdminAnalyticsSnapshotEntity toEntity(AdminAnalyticsSnapshot domain) {
        return AdminAnalyticsSnapshotEntity.builder()
                .id(domain.getId())
                .snapshotDate(domain.getSnapshotDate())
                .totalPatches(domain.getTotalPatches())
                .activeUsers(domain.getActiveUsers())
                .topCategories("[]")
                .retentionRate(domain.getRetentionRate())
                .generatedAt(domain.getGeneratedAt())
                .build();
    }
}
