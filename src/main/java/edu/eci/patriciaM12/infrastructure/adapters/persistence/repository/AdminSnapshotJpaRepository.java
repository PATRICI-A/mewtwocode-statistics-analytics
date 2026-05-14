package edu.eci.patriciaM12.infrastructure.adapters.persistence.repository;

import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.AdminAnalyticsSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link AdminAnalyticsSnapshotEntity}.
 * Provides derived query methods for date-based lookup of admin analytics snapshots.
 */
public interface AdminSnapshotJpaRepository extends JpaRepository<AdminAnalyticsSnapshotEntity, UUID> {

    /**
     * Finds the snapshot recorded on the exact given date.
     *
     * @param snapshotDate the date to search for
     * @return an {@link Optional} containing the matching entity, or empty if none exists
     */
    Optional<AdminAnalyticsSnapshotEntity> findBySnapshotDate(LocalDate snapshotDate);

    /**
     * Finds all snapshots recorded within the given date range, ordered by snapshot date ascending.
     *
     * @param dateFrom the start of the range (inclusive)
     * @param dateTo   the end of the range (inclusive)
     * @return a list of matching entities ordered chronologically; never {@code null}
     */
    List<AdminAnalyticsSnapshotEntity> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate dateFrom, LocalDate dateTo);
}
