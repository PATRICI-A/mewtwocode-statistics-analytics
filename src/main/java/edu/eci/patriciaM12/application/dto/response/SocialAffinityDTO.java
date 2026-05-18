package edu.eci.patriciaM12.application.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Represents the computed social affinity score for a student (RF-38 RN-38.5).
 * <p>
 * The total score is a weighted composite of three dimensions:
 * <ul>
 *   <li>Shared interests — 40 % weight</li>
 *   <li>Common parches attended — 35 % weight</li>
 *   <li>Mutual peer connections — 25 % weight</li>
 * </ul>
 * All individual scores are in the range [0.0, 1.0]; the total score is also in [0.0, 1.0].
 * </p>
 */
@Value
@Builder
public class SocialAffinityDTO {

    /**
     * Normalised score derived from shared interest tags (PatchCategory overlap).
     * Contributes 40 % to the total score.
     */
    Double sharedInterestsScore;

    /**
     * Normalised score derived from parches attended in common with peers.
     * Contributes 35 % to the total score.
     */
    Double commonParchesScore;

    /**
     * Normalised score derived from mutual peer connections.
     * Contributes 25 % to the total score.
     */
    Double mutualConnectionsScore;

    /**
     * Weighted composite affinity score.
     * Formula: {@code sharedInterests×0.40 + commonParches×0.35 + mutualConnections×0.25}.
     */
    Double totalScore;
}
