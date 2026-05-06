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

@Component
@RequiredArgsConstructor
public class StudentMetricsRepositoryAdapter implements StudentMetricsRepositoryPort {

    private final StudentMetricsJpaRepository jpaRepository;
    private final StudentMetricsMapper mapper;

    @Override
    public Optional<StudentDashboardMetric> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public StudentDashboardMetric save(StudentDashboardMetric metric) {
        StudentDashboardMetricEntity entity = mapper.toEntity(metric);
        return mapper.toDomain(jpaRepository.save(entity));
    }
}