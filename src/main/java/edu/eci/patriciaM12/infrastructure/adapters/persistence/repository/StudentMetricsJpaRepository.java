package edu.eci.patriciaM12.infrastructure.adapters.persistence.repository;

import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.StudentDashboardMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link StudentDashboardMetricEntity}.
 * Provides derived and JPQL query methods for retrieving and aggregating student
 * activity metrics, including week-range queries required by RF-38.
 */
public interface StudentMetricsJpaRepository extends JpaRepository<StudentDashboardMetricEntity, UUID> {

    /**
     * Finds the most recent dashboard metric entity for the given student.
     *
     * @param userId the UUID of the student
     * @return an {@link Optional} containing the entity if found, or empty otherwise
     */
    Optional<StudentDashboardMetricEntity> findByUserId(UUID userId);

    /**
     * Returns the total number of patches attended by the given student across all recorded periods.
     *
     * @param userId the UUID of the student
     * @return the sum of {@code patchesAttended}, or {@code null} if no records exist
     */
    @Query("SELECT SUM(s.patchesAttended) FROM StudentDashboardMetricEntity s WHERE s.userId = :userId")
    Integer sumPatchesAttendedByUserId(@Param("userId") UUID userId);

    /**
     * Returns the top patch category of the given student based on the most recent period.
     *
     * @param userId the UUID of the student
     * @return an {@link Optional} containing the category name string, or empty if no records exist
     */
    @Query("SELECT s.topCategory FROM StudentDashboardMetricEntity s WHERE s.userId = :userId ORDER BY s.period DESC LIMIT 1")
    Optional<String> findTopCategoryByUserId(@Param("userId") UUID userId);

    /**
     * Finds the dashboard metric for a student whose {@code period} falls within the given
     * week boundaries (RF-38).  Returns the most recent record within the range.
     *
     * @param userId    the UUID of the student
     * @param weekStart Monday of the target week (inclusive)
     * @param weekEnd   Sunday of the target week (inclusive)
     * @return an {@link Optional} containing the entity if found, or empty otherwise
     */
    @Query("SELECT s FROM StudentDashboardMetricEntity s " +
            "WHERE s.userId = :userId AND s.period BETWEEN :weekStart AND :weekEnd " +
            "ORDER BY s.period DESC LIMIT 1")
    Optional<StudentDashboardMetricEntity> findByUserIdAndWeekRange(
            @Param("userId") UUID userId,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd);
}
