package edu.eci.patriciaM12.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "SocialAffinity",
        description = """
                Computed social affinity score for a student (RF-38 RN-38.5). The total score is a \
                weighted composite: shared interests (40%), common parches attended (35%), and \
                mutual peer connections (25%). All scores are normalised to [0.0, 1.0]."""
)
public class SocialAffinityDTO {

    @Schema(
            description = "Normalised score derived from shared interest tags (PatchCategory overlap). Contributes 40% to total.",
            example = "0.85",
            minimum = "0",
            maximum = "1"
    )
    Double sharedInterestsScore;

    @Schema(
            description = "Normalised score derived from parches attended in common with peers. Contributes 35% to total.",
            example = "0.72",
            minimum = "0",
            maximum = "1"
    )
    Double commonParchesScore;

    @Schema(
            description = "Normalised score derived from mutual peer connections. Contributes 25% to total.",
            example = "0.68",
            minimum = "0",
            maximum = "1"
    )
    Double mutualConnectionsScore;

    @Schema(
            description = "Weighted composite affinity score. Formula: sharedInterests×0.40 + commonParches×0.35 + mutualConnections×0.25",
            example = "0.764",
            minimum = "0",
            maximum = "1"
    )
    Double totalScore;
}