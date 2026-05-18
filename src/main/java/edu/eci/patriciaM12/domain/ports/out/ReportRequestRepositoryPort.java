package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.ReportRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port (secondary port) that abstracts persistence operations for
 * {@link ReportRequest} records (RF-19).  Infrastructure adapters (e.g., a JPA
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

    /**
     * Returns all report requests submitted by the given user, ordered by creation date
     * descending.  Used to populate the report history list (RF-19 RN-19.5).
     *
     * @param requestedBy the UUID of the user whose requests are to be retrieved
     * @return list of report requests; may be empty; never {@code null}
     */
    List<ReportRequest> findAllByRequestedBy(UUID requestedBy);
}
