package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.application.dto.response.SocialIndicatorsResponse;

import java.util.UUID;

/**
 * Input port (primary port) for retrieving a student's social indicators (RF-38).
 * <p>
 * Returns the weekly participation summary, network growth, social affinity score, and
 * qualitative activity level for the authenticated student.  The implementation is
 * provided by {@code SocialIndicatorsService} in the application layer.
 * </p>
 */
public interface GetSocialIndicatorsUseCase {

    /**
     * Computes and returns the social indicators for the given student and week range.
     *
     * @param userId    the UUID of the authenticated student
     * @param weekRange optional week offset relative to the current week
     *                  (0 = current week, 1 = last week, etc.); defaults to 0 when {@code null}
     * @return the computed {@link SocialIndicatorsResponse}; never {@code null}
     */
    SocialIndicatorsResponse execute(UUID userId, Integer weekRange);
}
