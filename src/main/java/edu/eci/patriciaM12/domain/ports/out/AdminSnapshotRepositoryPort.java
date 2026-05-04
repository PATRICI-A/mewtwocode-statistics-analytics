package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdminSnapshotRepositoryPort {
    Optional<AdminAnalyticsSnapshot> findByDate(LocalDate date);
    List<AdminAnalyticsSnapshot> findByDateRange(LocalDate dateFrom, LocalDate dateTo);
    AdminAnalyticsSnapshot save(AdminAnalyticsSnapshot snapshot);
}