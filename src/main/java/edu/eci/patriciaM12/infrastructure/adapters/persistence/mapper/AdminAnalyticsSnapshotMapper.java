package edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper;

import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.AdminAnalyticsSnapshotEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AdminAnalyticsSnapshotMapper {

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
