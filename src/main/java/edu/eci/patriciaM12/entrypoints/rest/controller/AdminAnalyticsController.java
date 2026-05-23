package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
import edu.eci.patriciaM12.domain.model.enums.MetricType;
import edu.eci.patriciaM12.domain.ports.in.GetAdminAnalyticsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST controller that exposes the admin analytics panel endpoint (RF-18).
 * All routes under {@code /api/v1/analytics/admin} require the {@code ADMINISTRADOR} role
 * (bypassed in the {@code dev} Spring profile for convenience).
 */
@RestController
@RequestMapping("/api/v1/analytics/admin")
@RequiredArgsConstructor
@Tag(
        name = "Admin Analytics",
        description = """
                Provides the global analytics panel exclusively for users with the `ADMINISTRADOR` role (RF-18). \
                Exposes aggregated platform metrics including active users, parche statistics, event rankings, \
                match success rates, campus heatmaps, retention figures, and automated alerts for significant \
                metric drops. All data is scoped to a configurable date range and can be filtered by metric \
                category and/or faculty.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class AdminAnalyticsController {

    private final GetAdminAnalyticsUseCase getAdminAnalyticsUseCase;

    @GetMapping
    @Operation(
            operationId = "getAdminAnalyticsPanel",
            summary = "Retrieve the global admin analytics panel",
            description = """
                    Returns a comprehensive analytics dashboard intended for platform administrators. \
                    The response aggregates up to nine metric categories depending on the filters applied:

                    - **Active users** — time-series of daily/weekly active users (RN-18.1)
                    - **Parche stats** — total and per-category parche counts (RN-18.2)
                    - **Top events** — ranked list of the most-attended events (RN-18.3)
                    - **Match success rate** — percentage of confirmed parche matches (RN-18.4)
                    - **Campus heatmap** — activity density per zone and hour (RN-18.5)
                    - **Retention rate** — users active in both the current and previous period (RN-18.7)
                    - **Abandoned parches** — parches cancelled or left without members (RN-18.8)
                    - **Avg. time to first member** — minutes until a parche gets its first join (RN-18.9)
                    - **Alerts** — metrics that dropped more than 30 % versus the prior week (RN-18.6)

                    **Date range behaviour:** when `startDate` or `endDate` are omitted, the boundaries \
                    of the active academic semester are used automatically.

                    **Metric filter behaviour:** when `metricType` is omitted, all nine metric categories \
                    are included in the response. Supplying a specific value (e.g. `USERS`) restricts the \
                    response to that category only; all other fields are omitted (`null`) from the JSON output.

                    **Faculty filter behaviour:** when `facultyFilter` is omitted, analytics cover all \
                    faculties. Providing a faculty name (e.g. `"Ingeniería"`) restricts the scope to \
                    students and events belonging to that faculty (RN-18.10).

                    This endpoint requires the `ADMINISTRADOR` JWT role. In the `dev` Spring profile \
                    the security filter is permissive for this path for local testing convenience.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Analytics panel retrieved successfully. The response body contains all \
                            requested metric categories. Fields filtered out by `metricType` are \
                            omitted from the JSON response (not present, not null).
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminAnalyticsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Invalid query parameters. Common causes: `startDate` is after `endDate`, \
                            date values do not conform to ISO-8601 format (yyyy-MM-dd), or `metricType` \
                            contains an unrecognised value. The response body includes a validation \
                            error message.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            The request did not include a valid JWT Bearer token, or the token has \
                            expired. Re-authenticate and retry with a fresh token.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            The authenticated user does not have the `ADMINISTRADOR` role required \
                            to access this endpoint. Ensure the correct account and role are being used.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            An unexpected server-side error occurred while aggregating the analytics \
                            data. Retry the request; if the problem persists, contact the platform \
                            operations team.
                            """,
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<AdminAnalyticsResponse> getAdminAnalyticsPanel(
            @Parameter(
                    description = """
                            Inclusive start date of the analytics window in ISO-8601 format \
                            (`yyyy-MM-dd`). When omitted, defaults to the first day of the \
                            active academic semester.
                            """,
                    example = "2025-02-01"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(
                    description = """
                            Inclusive end date of the analytics window in ISO-8601 format \
                            (`yyyy-MM-dd`). When omitted, defaults to the last day of the \
                            active academic semester.
                            """,
                    example = "2025-06-30"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(
                    description = """
                            Restricts the response to a single metric category. Accepted values: \
                            `USERS`, `PARCHES`, `EVENTS`, `MATCHES`, `ZONES`. \
                            When omitted, all categories are returned.
                            """,
                    schema = @Schema(implementation = MetricType.class)
            )
            @RequestParam(required = false) MetricType metricType,

            @Parameter(
                    description = """
                            Filters analytics to a specific faculty by name (e.g. `"Ingeniería"`). \
                            The comparison is case-insensitive. When omitted, data from all \
                            faculties is included (RN-18.10).
                            """,
                    example = "Ingeniería"
            )
            @RequestParam(required = false) String facultyFilter) {

        return ResponseEntity.ok(
                getAdminAnalyticsUseCase.getPanel(startDate, endDate, metricType, facultyFilter));
    }
}