package edu.eci.patriciaM12.infrastructure.adapters.persistence.repository;

import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.ReportRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ReportRequestEntity}.
 * Inherits standard CRUD and pagination operations from {@link JpaRepository}.
 * Custom query methods can be added here as the reporting feature evolves.
 */
public interface ReportRequestJpaRepository extends JpaRepository<ReportRequestEntity, UUID> {
}