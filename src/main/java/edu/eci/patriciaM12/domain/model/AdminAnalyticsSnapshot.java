package edu.eci.patriciaM12.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Getter
@Builder
public class AdminAnalyticsSnapshot {

    private UUID id;
    private LocalDate snapshotDate;
    private int totalPatches;
    private int activeUsers;
    private List<CategoryStat> topCategories;
    private Map<Integer, Integer> peakHours;
    private float retentionRate;
    private LocalDateTime generatedAt;
}