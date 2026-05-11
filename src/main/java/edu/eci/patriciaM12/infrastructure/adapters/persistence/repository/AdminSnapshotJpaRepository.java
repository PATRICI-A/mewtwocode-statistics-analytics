package edu.eci.patriciaM12.infrastructure.adapters.persistence.repository;

import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.AdminAnalyticsSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminSnapshotJpaRepository extends JpaRepository<AdminAnalyticsSnapshotEntity, UUID> {
    Optional<AdminAnalyticsSnapshotEntity> findBySnapshotDate(LocalDate snapshotDate);
    List<AdminAnalyticsSnapshotEntity> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate dateFrom, LocalDate dateTo);
}
