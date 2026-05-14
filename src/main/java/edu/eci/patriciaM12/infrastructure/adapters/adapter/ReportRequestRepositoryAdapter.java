package edu.eci.patriciaM12.infrastructure.adapters.adapter;

import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.ports.out.ReportRequestRepositoryPort;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper.ReportRequestMapper;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.repository.ReportRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements {@link ReportRequestRepositoryPort} using Spring Data JPA.
 * Bridges the domain port with {@link ReportRequestJpaRepository} and handles entity-to-domain
 * conversion via {@link ReportRequestMapper}.
 */
@Component
@RequiredArgsConstructor
public class ReportRequestRepositoryAdapter implements ReportRequestRepositoryPort {

    private final ReportRequestJpaRepository jpaRepository;
    private final ReportRequestMapper mapper;

    /**
     * Persists the given report request, inserting or updating as appropriate.
     *
     * @param reportRequest the domain report request to persist
     * @return the persisted domain report request with any database-generated values populated
     */
    @Override
    public ReportRequest save(ReportRequest reportRequest) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(reportRequest)));
    }

    /**
     * Retrieves a report request by its unique identifier.
     *
     * @param id the UUID of the report request to retrieve
     * @return an {@link Optional} containing the domain report request if found, or empty otherwise
     */
    @Override
    public Optional<ReportRequest> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}