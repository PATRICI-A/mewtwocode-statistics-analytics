package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Output port (secondary port) that abstracts persistence operations for
 * {@link AdminAnalyticsSnapshot} records (RF-18).  Infrastructure adapters (e.g., JPA
 * repositories) must implement this interface to satisfy the domain's storage needs.
 */
public interface AdminSnapshotRepositoryPort {

    /**
     * Looks up the admin analytics snapshot for a specific calendar date.
     *
     * @param date the date for which the snapshot is requested
     * @return an {@link Optional} containing the snapshot if one exists, or empty if not
     */
    Optional<AdminAnalyticsSnapshot> findByDate(LocalDate date);

    /**
     * Retrieves all admin analytics snapshots whose {@code snapshotDate} falls within
     * the given inclusive date range, ordered chronologically.
     *
     * @param dateFrom inclusive start date of the range
     * @param dateTo   inclusive end date of the range
     * @return list of matching snapshots; may be empty if none exist in the range
     */
    List<AdminAnalyticsSnapshot> findByDateRange(LocalDate dateFrom, LocalDate dateTo);

    /**
     * Retrieves admin analytics snapshots within the given date range filtered by faculty
     * name (RF-18 RN-18.10).  When {@code facultyFilter} is {@code null} or blank, the
     * result is equivalent to {@link #findByDateRange(LocalDate, LocalDate)}.
     *
     * @param dateFrom      inclusive start date of the range
     * @param dateTo        inclusive end date of the range
     * @param facultyFilter the faculty name to restrict results; {@code null} means all faculties
     * @return list of matching snapshots; may be empty if none exist
     */
    List<AdminAnalyticsSnapshot> findByDateRangeAndFaculty(LocalDate dateFrom, LocalDate dateTo, String facultyFilter);

    /**
     * Persists a new or updated {@link AdminAnalyticsSnapshot}.
     *
     * @param snapshot the snapshot to save; must not be {@code null}
     * @return the saved snapshot, potentially with infrastructure-assigned metadata
     */
    AdminAnalyticsSnapshot save(AdminAnalyticsSnapshot snapshot);
}
