package edu.eci.patriciaM12.infrastructure.adapters.adapter;

import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.StudentDashboardMetricEntity;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper.StudentMetricsMapper;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.repository.StudentMetricsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements {@link StudentMetricsRepositoryPort} using Spring Data JPA.
 * Bridges the domain port with {@link StudentMetricsJpaRepository} and handles entity-to-domain
 * conversion via {@link StudentMetricsMapper}.
 */
@Component
@RequiredArgsConstructor
public class StudentMetricsRepositoryAdapter implements StudentMetricsRepositoryPort {

    private final StudentMetricsJpaRepository jpaRepository;
    private final StudentMetricsMapper mapper;

    /**
     * Retrieves the dashboard metric record for the given student.
     *
     * @param userId the UUID of the student whose metrics are requested
     * @return an {@link Optional} containing the domain metric if found, or empty otherwise
     */
    @Override
    public Optional<StudentDashboardMetric> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    /**
     * Persists the given student dashboard metric, inserting or updating as appropriate.
     *
     * @param metric the domain metric to persist
     * @return the persisted domain metric with any database-generated values populated
     */
    @Override
    public StudentDashboardMetric save(StudentDashboardMetric metric) {
        StudentDashboardMetricEntity entity = mapper.toEntity(metric);
        return mapper.toDomain(jpaRepository.save(entity));
    }
}