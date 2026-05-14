package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.ReportRequest;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port (secondary port) that abstracts persistence operations for
 * {@link ReportRequest} records.  Infrastructure adapters (e.g., a MongoDB or JPA
 * repository) must implement this interface so the domain can store and retrieve
 * asynchronous report requests without depending on any specific technology.
 */
public interface ReportRequestRepositoryPort {

    /**
     * Persists a new or updated {@link ReportRequest}.
     *
     * @param reportRequest the report request to save; must not be {@code null}
     * @return the saved report request, potentially enriched with infrastructure-assigned metadata
     */
    ReportRequest save(ReportRequest reportRequest);

    /**
     * Looks up a report request by its unique identifier.
     *
     * @param id the unique identifier of the report request to retrieve
     * @return an {@link Optional} containing the report request if found, or empty if not
     */
    Optional<ReportRequest> findById(UUID id);
}