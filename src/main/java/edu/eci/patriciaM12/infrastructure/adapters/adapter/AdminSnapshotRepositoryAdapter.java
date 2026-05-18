package edu.eci.patriciaM12.infrastructure.adapters.adapter;

import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.ports.out.AdminSnapshotRepositoryPort;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.AdminAnalyticsSnapshotEntity;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper.AdminAnalyticsSnapshotMapper;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.repository.AdminSnapshotJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Infrastructure adapter that implements {@link AdminSnapshotRepositoryPort} using Spring Data JPA.
 * Bridges the domain port with {@link AdminSnapshotJpaRepository} and handles entity-to-domain
 * conversion via {@link AdminAnalyticsSnapshotMapper} (RF-18).
 */
@Component
@RequiredArgsConstructor
public class AdminSnapshotRepositoryAdapter implements AdminSnapshotRepositoryPort {

    private final AdminSnapshotJpaRepository jpaRepository;
    private final AdminAnalyticsSnapshotMapper mapper;

    /**
     * Finds the admin analytics snapshot recorded on the exact given date.
     *
     * @param date the snapshot date to look up
     * @return an {@link Optional} containing the domain snapshot if found, or empty otherwise
     */
    @Override
    public Optional<AdminAnalyticsSnapshot> findByDate(LocalDate date) {
        return jpaRepository.findBySnapshotDate(date).map(mapper::toDomain);
    }

    /**
     * Finds all admin analytics snapshots recorded within the given date range, ordered by date ascending.
     *
     * @param dateFrom the start of the date range (inclusive)
     * @param dateTo   the end of the date range (inclusive)
     * @return an ordered list of domain snapshots; never {@code null}
     */
    @Override
    public List<AdminAnalyticsSnapshot> findByDateRange(LocalDate dateFrom, LocalDate dateTo) {
        return jpaRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(dateFrom, dateTo)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Finds admin analytics snapshots within the given date range filtered by faculty name.
     * When {@code facultyFilter} is {@code null} or blank, the result is equivalent to
     * {@link #findByDateRange(LocalDate, LocalDate)}.
     *
     * @param dateFrom      inclusive start date
     * @param dateTo        inclusive end date
     * @param facultyFilter optional faculty name; {@code null} means all faculties
     * @return an ordered list of domain snapshots; never {@code null}
     */
    @Override
    public List<AdminAnalyticsSnapshot> findByDateRangeAndFaculty(LocalDate dateFrom, LocalDate dateTo,
                                                                    String facultyFilter) {
        String faculty = (facultyFilter != null && !facultyFilter.isBlank()) ? facultyFilter : null;
        return jpaRepository.findByDateRangeAndFaculty(dateFrom, dateTo, faculty)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Persists the given admin analytics snapshot, inserting or updating as appropriate.
     *
     * @param snapshot the domain snapshot to persist
     * @return the persisted domain snapshot with any database-generated values populated
     */
    @Override
    public AdminAnalyticsSnapshot save(AdminAnalyticsSnapshot snapshot) {
        AdminAnalyticsSnapshotEntity entity = mapper.toEntity(snapshot);
        return mapper.toDomain(jpaRepository.save(entity));
    }
}
