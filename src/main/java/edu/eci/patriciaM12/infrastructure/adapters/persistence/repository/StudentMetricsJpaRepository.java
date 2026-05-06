package edu.eci.patriciaM12.infrastructure.adapters.persistence.repository;

import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.StudentDashboardMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StudentMetricsJpaRepository extends JpaRepository<StudentDashboardMetricEntity, UUID> {

    Optional<StudentDashboardMetricEntity> findByUserId(UUID userId);

    @Query("SELECT SUM(s.patchesAttended) FROM StudentDashboardMetricEntity s WHERE s.userId = :userId")
    Integer sumPatchesAttendedByUserId(@Param("userId") UUID userId);

    @Query("SELECT s.topCategory FROM StudentDashboardMetricEntity s WHERE s.userId = :userId ORDER BY s.period DESC LIMIT 1")
    Optional<String> findTopCategoryByUserId(@Param("userId") UUID userId);
}