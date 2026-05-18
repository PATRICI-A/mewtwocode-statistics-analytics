package edu.eci.patriciaM12.infrastructure.adapters.persistence.repository;

import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.ReportRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ReportRequestEntity} (RF-19).
 * Provides CRUD operations and a user-scoped history query.
 */
public interface ReportRequestJpaRepository extends JpaRepository<ReportRequestEntity, UUID> {

    /**
     * Returns all report requests submitted by the given user, ordered by creation date descending.
     * Used to populate the report history list (RF-19 RN-19.5).
     *
     * @param requestedBy the UUID of the user whose requests are to be retrieved
     * @return list of matching entities; may be empty; never {@code null}
     */
    List<ReportRequestEntity> findByRequestedByOrderByCreatedAtDesc(UUID requestedBy);
}
