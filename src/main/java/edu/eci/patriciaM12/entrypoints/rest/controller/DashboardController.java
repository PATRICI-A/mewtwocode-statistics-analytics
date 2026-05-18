package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.response.InteractionAnalyticsResponse;
import edu.eci.patriciaM12.application.dto.response.SocialIndicatorsResponse;
import edu.eci.patriciaM12.application.dto.response.StudentDashboardResponse;
import edu.eci.patriciaM12.domain.ports.in.GetInteractionAnalyticsUseCase;
import edu.eci.patriciaM12.domain.ports.in.GetSocialIndicatorsUseCase;
import edu.eci.patriciaM12.domain.ports.in.GetStudentDashboardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller that exposes the student personal dashboard and social analytics endpoints.
 * The student identity is resolved from the JWT principal; no explicit user ID is required
 * in the request.  All endpoints always return HTTP 200 (Empty Object Pattern).
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Personal student dashboard and social analytics")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final GetStudentDashboardUseCase getStudentDashboardUseCase;
    private final GetSocialIndicatorsUseCase getSocialIndicatorsUseCase;
    private final GetInteractionAnalyticsUseCase getInteractionAnalyticsUseCase;

    /**
     * Returns the personal activity metrics for the authenticated student.
     * If no metrics have been persisted yet a synthetic zeroed-out response is returned
     * (HTTP 200, never 404).
     *
     * @param jwt the JWT token injected by Spring Security; the subject claim is used as the user ID
     * @return HTTP 200 with the student's {@link StudentDashboardResponse}
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Returns personal metrics for the authenticated student")
    public ResponseEntity<StudentDashboardResponse> getDashboard(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(
                StudentDashboardResponse.from(getStudentDashboardUseCase.execute(userId)));
    }

    /**
     * Returns the social indicators for the authenticated student for the requested week (RF-38).
     * <p>
     * Includes weekly participation, network growth, social affinity score (weighted composite),
     * and qualitative activity level.  Always returns HTTP 200; zeroed-out when no data exists.
     * </p>
     *
     * @param jwt       the JWT token; the subject claim is used as the user ID
     * @param weekRange optional week offset (0 = current week, 1 = last week, …); defaults to 0
     * @return HTTP 200 with the computed {@link SocialIndicatorsResponse}
     */
    @GetMapping("/social-indicators")
    @Operation(summary = "Returns social indicators for the authenticated student (RF-38)")
    public ResponseEntity<SocialIndicatorsResponse> getSocialIndicators(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer weekRange) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(getSocialIndicatorsUseCase.execute(userId, weekRange));
    }

    /**
     * Returns the interaction analytics for the authenticated student (RF-39).
     * <p>
     * Aggregates total interactions, most-active campus zone, peak activity day, and a
     * breakdown by interaction type.  Always returns HTTP 200; zeroed-out when no data exists.
     * </p>
     *
     * @param jwt the JWT token; the subject claim is used as the user ID
     * @return HTTP 200 with the computed {@link InteractionAnalyticsResponse}
     */
    @GetMapping("/interaction-analytics")
    @Operation(summary = "Returns interaction analytics for the authenticated student (RF-39)")
    public ResponseEntity<InteractionAnalyticsResponse> getInteractionAnalytics(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(getInteractionAnalyticsUseCase.execute(userId));
    }
}
