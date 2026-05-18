package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.application.dto.response.InteractionAnalyticsResponse;

import java.util.UUID;

/**
 * Input port (primary port) for retrieving a student's interaction analytics (RF-39).
 * <p>
 * Aggregates total interaction counts, most-active campus zone, peak activity day, and a
 * type-breakdown map for the authenticated student.  The implementation is provided by
 * {@code InteractionAnalyticsService} in the application layer.
 * </p>
 */
public interface GetInteractionAnalyticsUseCase {

    /**
     * Computes and returns the interaction analytics for the given student.
     *
     * @param userId the UUID of the authenticated student
     * @return the computed {@link InteractionAnalyticsResponse}; never {@code null}
     */
    InteractionAnalyticsResponse execute(UUID userId);
}
