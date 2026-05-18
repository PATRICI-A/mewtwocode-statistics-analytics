package edu.eci.patriciaM12.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.eci.patriciaM12.domain.model.enums.ActivityLevel;
import lombok.Builder;
import lombok.Value;

/**
 * Top-level response returned by the social indicators endpoint (RF-38).
 * <p>
 * Contains the student's weekly participation summary, network growth compared to the
 * previous week, social affinity score, and a qualitative activity level classification.
 * Fields are omitted from the JSON response when {@code null}.
 * </p>
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SocialIndicatorsResponse {

    /** Weekly parche attendance, RSVP confirmations, and active connection counts. */
    WeeklyParticipationDTO weeklyParticipation;

    /** Comparison of peer connections between the current and previous week. */
    NetworkGrowthDTO networkGrowth;

    /**
     * Weighted social affinity score composed of shared interests (40 %),
     * common parches (35 %), and mutual connections (25 %).
     */
    SocialAffinityDTO socialAffinity;

    /** Qualitative classification of the student's activity level for the week. */
    ActivityLevel activityLevel;
}
