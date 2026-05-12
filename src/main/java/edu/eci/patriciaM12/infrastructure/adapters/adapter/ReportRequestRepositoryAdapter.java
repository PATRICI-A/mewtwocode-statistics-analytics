package edu.eci.patriciaM12.infrastructure.adapters.adapter;

import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.ports.out.ReportRequestRepositoryPort;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper.ReportRequestMapper;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.repository.ReportRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportRequestRepositoryAdapter implements ReportRequestRepositoryPort {

    private final ReportRequestJpaRepository jpaRepository;
    private final ReportRequestMapper mapper;

    @Override
    public ReportRequest save(ReportRequest reportRequest) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(reportRequest)));
    }

    @Override
    public Optional<ReportRequest> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}