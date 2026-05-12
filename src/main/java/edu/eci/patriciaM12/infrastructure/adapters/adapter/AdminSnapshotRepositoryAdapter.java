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

@Component
@RequiredArgsConstructor
public class AdminSnapshotRepositoryAdapter implements AdminSnapshotRepositoryPort {

    private final AdminSnapshotJpaRepository jpaRepository;
    private final AdminAnalyticsSnapshotMapper mapper;

    @Override
    public Optional<AdminAnalyticsSnapshot> findByDate(LocalDate date) {
        return jpaRepository.findBySnapshotDate(date).map(mapper::toDomain);
    }

    @Override
    public List<AdminAnalyticsSnapshot> findByDateRange(LocalDate dateFrom, LocalDate dateTo) {
        return jpaRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(dateFrom, dateTo)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public AdminAnalyticsSnapshot save(AdminAnalyticsSnapshot snapshot) {
        AdminAnalyticsSnapshotEntity entity = mapper.toEntity(snapshot);
        return mapper.toDomain(jpaRepository.save(entity));
    }
}
