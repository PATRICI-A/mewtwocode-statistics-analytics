package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.application.dto.response.InstitutionalStatsResponse;
import edu.eci.patriciaM12.domain.model.enums.InstitutionalMetricType;

import java.time.LocalDate;

/**
 * Input port (primary port) for retrieving institutional-level statistics accessible to
 * Bienestar role users (RF-40).
 * <p>
 * Returns event lifecycle statistics, student participation figures, and a composite social
 * activity index.  The implementation is provided by {@code InstitutionalStatsService} in
 * the application layer.
 * </p>
 */
public interface GetInstitutionalStatsUseCase {

    /**
     * Computes and returns institutional statistics for the given date range and metric filter.
     *
     * @param startDate  inclusive start of the observed period; {@code null} defaults to semester start
     * @param endDate    inclusive end of the observed period; {@code null} defaults to semester end
     * @param metricType the category of metrics to include; {@link InstitutionalMetricType#ALL} returns everything
     * @return the computed {@link InstitutionalStatsResponse}; never {@code null}
     */
    InstitutionalStatsResponse execute(LocalDate startDate, LocalDate endDate, InstitutionalMetricType metricType);
}
