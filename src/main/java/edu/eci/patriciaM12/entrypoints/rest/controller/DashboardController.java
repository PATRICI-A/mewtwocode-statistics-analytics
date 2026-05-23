package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.response.InteractionAnalyticsResponse;
import edu.eci.patriciaM12.application.dto.response.SocialIndicatorsResponse;
import edu.eci.patriciaM12.application.dto.response.StudentDashboardResponse;
import edu.eci.patriciaM12.domain.ports.in.GetInteractionAnalyticsUseCase;
import edu.eci.patriciaM12.domain.ports.in.GetSocialIndicatorsUseCase;
import edu.eci.patriciaM12.domain.ports.in.GetStudentDashboardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller that exposes the student personal dashboard and social analytics endpoints.
 * The student identity is resolved from the JWT principal; no explicit user ID is required
 * in the request. All endpoints always return HTTP 200 (Empty Object Pattern).
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(
        name = "Dashboard",
        description = """
                Provides personal analytics dashboards and social metrics for authenticated students. \
                All three endpoints resolve the student identity directly from the JWT token — \
                no user ID is required in the request. Following the Empty Object Pattern, \
                all endpoints always return HTTP 200 even when no data has been recorded yet; \
                in that case, a zeroed-out response object is returned instead of 404 or 204.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final GetStudentDashboardUseCase getStudentDashboardUseCase;
    private final GetSocialIndicatorsUseCase getSocialIndicatorsUseCase;
    private final GetInteractionAnalyticsUseCase getInteractionAnalyticsUseCase;

    @GetMapping("/dashboard")
    @Operation(
            operationId = "getStudentDashboard",
            summary = "Retrieve the personal activity dashboard for the authenticated student",
            description = """
                    Returns the personal activity metrics dashboard for the student identified \
                    by the JWT `sub` claim. The dashboard aggregates all recorded activity for \
                    that student within the platform — parche participation, event RSVPs, social \
                    connections, and other engagement indicators — into a single structured response.

                    **Identity resolution:** the student's UUID is extracted from the JWT `subject` \
                    claim automatically. No user ID parameter is needed or accepted.

                    **Empty Object Pattern:** if the student has no recorded activity yet (e.g. a \
                    newly registered account), the endpoint still returns HTTP 200 with all numeric \
                    fields set to zero and list fields set to empty collections. It never returns \
                    HTTP 404 for a valid authenticated user.

                    This endpoint is intended for the student-facing mobile/web dashboard screen \
                    and should be called on page load to populate all personal metric cards.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Dashboard data retrieved successfully. If the student has no activity \
                            recorded yet, all numeric metrics are returned as zero and lists \
                            as empty — never a 404 or empty body.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentDashboardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            No valid JWT Bearer token was provided, or the token has expired. \
                            The client must re-authenticate before retrying this request.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            The authenticated principal does not have the `ESTUDIANTE` role \
                            required to access personal dashboard data.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            An unexpected error occurred while retrieving the dashboard data. \
                            Retry the request; if the problem persists, contact support.
                            """,
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<StudentDashboardResponse> getDashboard(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(
                StudentDashboardResponse.from(getStudentDashboardUseCase.execute(userId)));
    }

    @GetMapping("/social-indicators")
    @Operation(
            operationId = "getSocialIndicators",
            summary = "Retrieve weekly social indicators for the authenticated student",
            description = """
                    Returns the social engagement indicators for the authenticated student \
                    for a specific week (RF-38). The response includes:

                    - **Weekly participation** — number of parches and events the student \
                      attended or joined during the requested week
                    - **Network growth** — new social connections (follows, matches) established \
                      in the week
                    - **Social affinity score** — a weighted composite index calculated as: \
                      `participation×0.40 + network_growth×0.35 + event_rsvp×0.25`, normalised \
                      to a [0.0, 1.0] range
                    - **Activity level** — a qualitative label (`LOW`, `MEDIUM`, `HIGH`, \
                      `VERY_HIGH`) derived from the affinity score

                    **Week offset:** `weekRange=0` returns the current calendar week, \
                    `weekRange=1` returns last week, `weekRange=2` returns two weeks ago, and so on. \
                    When omitted, the current week is used by default.

                    **Empty Object Pattern:** always returns HTTP 200; if no data exists for \
                    the requested week, all numeric fields are zero and the activity level \
                    is `LOW`.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Social indicators retrieved successfully for the specified week. \
                            Returns zeroed-out values with `LOW` activity level if no data \
                            exists for that week — never a 404.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SocialIndicatorsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            The `weekRange` parameter is invalid (e.g. negative value or \
                            non-integer). Provide a non-negative integer value.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            No valid JWT Bearer token was provided, or the token has expired. \
                            Re-authenticate before retrying.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            The authenticated user lacks the `ESTUDIANTE` role required \
                            to access social indicator data.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            An unexpected server error occurred while computing social indicators. \
                            Retry the request or contact support if the issue persists.
                            """,
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<SocialIndicatorsResponse> getSocialIndicators(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,

            @Parameter(
                    description = """
                            Week offset relative to the current calendar week. \
                            `0` (default) = current week, `1` = last week, `2` = two weeks ago, etc. \
                            Must be a non-negative integer.
                            """,
                    example = "0",
                    schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            )
            @RequestParam(required = false) Integer weekRange) {
        return ResponseEntity.ok(getSocialIndicatorsUseCase.execute(userId, weekRange));
    }

    @GetMapping("/interaction-analytics")
    @Operation(
            operationId = "getInteractionAnalytics",
            summary = "Retrieve interaction analytics for the authenticated student",
            description = """
                    Returns aggregated interaction analytics for the authenticated student (RF-39). \
                    This endpoint analyses all recorded interactions — parche joins, event views, \
                    chat messages, campus check-ins, and recommendation clicks — and surfaces the \
                    following computed metrics:

                    - **Total interactions** — absolute count of all interaction events recorded \
                      for the student across the platform
                    - **Most-active campus zone** — the campus zone (e.g. `BIBLIOTECA`, `CAFETERIA`) \
                      where the student recorded the highest number of interactions
                    - **Peak activity day** — the day of the week (e.g. `MONDAY`, `WEDNESDAY`) on \
                      which the student is most active on average
                    - **Interaction breakdown** — a map of interaction type to count, allowing \
                      clients to display per-category bar charts or pie charts

                    **Identity resolution:** the student UUID is extracted from the JWT `sub` claim. \
                    No additional parameters are required.

                    **Empty Object Pattern:** if no interactions have been recorded for the student, \
                    the endpoint returns HTTP 200 with zero totals and null zone/day fields — \
                    never HTTP 404.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Interaction analytics retrieved successfully. Returns zeroed-out values \
                            if no interactions have been recorded yet — never a 404.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InteractionAnalyticsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            No valid JWT Bearer token was provided or the token is expired. \
                            Re-authenticate before retrying this request.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            The authenticated user does not have the `ESTUDIANTE` role required \
                            to access interaction analytics.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            An unexpected error occurred while aggregating interaction data. \
                            Retry the request or contact the platform operations team.
                            """,
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<InteractionAnalyticsResponse> getInteractionAnalytics(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(getInteractionAnalyticsUseCase.execute(userId));
    }
}