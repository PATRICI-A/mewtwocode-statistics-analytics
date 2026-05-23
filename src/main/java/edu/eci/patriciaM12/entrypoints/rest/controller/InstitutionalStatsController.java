package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.response.InstitutionalStatsResponse;
import edu.eci.patriciaM12.domain.model.enums.InstitutionalMetricType;
import edu.eci.patriciaM12.domain.ports.in.GetInstitutionalStatsUseCase;
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
 * REST controller that exposes the institutional statistics endpoint accessible to
 * users with the {@code BIENESTAR} role (RF-40).
 * <p>
 * Returns aggregated event lifecycle statistics, student participation figures,
 * and a composite social activity index for the campus.
 * </p>
 *
 * <p><strong>Security:</strong> This endpoint requires the {@code BIENESTAR} JWT role.
 * Unlike the admin analytics path, this endpoint is always protected — even in the
 * {@code dev} Spring profile.</p>
 */
@RestController
@RequestMapping("/api/v1/analytics/institutional")
@RequiredArgsConstructor
@Tag(
        name = "Institutional Statistics",
        description = """
                Provides campus-wide aggregated statistics exclusively for users with the `BIENESTAR` \
                role (RF-40). Exposes three categories of institutional metrics: event and parche \
                lifecycle statistics, student participation and RSVP figures, and a composite social \
                activity index for the campus. This endpoint is always protected — the `BIENESTAR` \
                JWT role is required even in the `dev` Spring profile.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class InstitutionalStatsController {

    private final GetInstitutionalStatsUseCase getInstitutionalStatsUseCase;

    @GetMapping
    @Operation(
            operationId = "getInstitutionalStats",
            summary = "Retrieve institutional campus-wide statistics for Bienestar users",
            description = """
                    Returns aggregated institutional statistics scoped to the requested date range \
                    and metric category (RF-40). This endpoint is designed for the Bienestar \
                    administrative panel and provides a high-level view of campus social activity.

                    The response can include up to three data sections depending on the `metricType` \
                    filter applied:

                    - **Events stats** (`EVENTS`) — aggregated counts of parche/event creation, \
                      publication, cancellation, and completion across the campus for the period. \
                      Useful for understanding the volume and health of the campus social calendar.

                    - **Participation stats** (`PARTICIPATION`) — student RSVP confirmations, \
                      actual attendance figures, and week-over-week participation trend. \
                      Useful for measuring how engaged students are with published events.

                    - **Social activity index** (`SOCIAL_ACTIVITY`) — a composite score computed as \
                      `parche_attendance×0.40 + connections×0.35 + event_rsvp×0.25`, normalised to \
                      [0.0, 1.0]. Provides a single KPI summarising overall campus social health \
                      (RN-40.3).

                    **Metric filter behaviour:** when `metricType` is `ALL` (default), all three \
                    sections are returned. Specifying a single value (e.g. `EVENTS`) returns only \
                    that section; the other two fields are omitted from the JSON response.

                    **Date range behaviour:** when `startDate` or `endDate` are absent, the \
                    boundaries of the active academic semester are used automatically.

                    **Security note:** this endpoint is always protected by the `BIENESTAR` role \
                    check regardless of the active Spring profile.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Institutional statistics retrieved successfully. Fields not covered \
                            by the applied `metricType` filter are omitted from the JSON response \
                            (not null — simply absent).
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InstitutionalStatsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            One or more query parameters are invalid. Common causes: `startDate` \
                            is after `endDate`, date values do not follow ISO-8601 format \
                            (`yyyy-MM-dd`), or `metricType` contains an unrecognised value. \
                            The response body contains a validation error message.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            No valid JWT Bearer token was included in the request, or the \
                            token has expired. Re-authenticate and retry with a fresh token.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            The authenticated user does not have the `BIENESTAR` role required \
                            to access institutional statistics. This restriction is enforced \
                            in all Spring profiles, including `dev`.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            An unexpected server-side error occurred while aggregating the \
                            institutional data. Retry the request; if the problem persists, \
                            contact the platform operations team.
                            """,
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<InstitutionalStatsResponse> getInstitutionalStats(
            @Parameter(
                    description = """
                            Inclusive start date of the observation window in ISO-8601 format \
                            (`yyyy-MM-dd`). When omitted, defaults to the first day of the \
                            active academic semester.
                            """,
                    example = "2025-02-01"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(
                    description = """
                            Inclusive end date of the observation window in ISO-8601 format \
                            (`yyyy-MM-dd`). When omitted, defaults to the last day of the \
                            active academic semester.
                            """,
                    example = "2025-06-30"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(
                    description = """
                            Category of institutional metrics to include in the response. \
                            Accepted values:
                            - `EVENTS` — parche/event lifecycle statistics only
                            - `PARTICIPATION` — student attendance and RSVP figures only
                            - `SOCIAL_ACTIVITY` — composite social activity index only
                            - `ALL` (default) — all three categories in a single response
                            """,
                    schema = @Schema(implementation = InstitutionalMetricType.class),
                    example = "ALL"
            )
            @RequestParam(required = false, defaultValue = "ALL") InstitutionalMetricType metricType) {

        return ResponseEntity.ok(
                getInstitutionalStatsUseCase.execute(startDate, endDate, metricType));
    }
}