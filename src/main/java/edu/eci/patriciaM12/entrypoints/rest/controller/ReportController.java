package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.request.ReportFiltersRequest;
import edu.eci.patriciaM12.application.dto.response.ReportHistoryDTO;
import edu.eci.patriciaM12.application.dto.response.ReportRequestResponse;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import edu.eci.patriciaM12.domain.ports.in.RequestReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller that manages CSV report generation, preview, history, and download (RF-19).
 * <p>
 * Report generation is asynchronous: a {@code POST} immediately returns HTTP 202 with a
 * {@code PENDING} status, and clients poll {@code GET /{id}/download} until the status
 * transitions to {@code READY} or {@code FAILED}.
 * When the request includes {@code "preview": true}, the first 10 rows are returned immediately
 * as {@code READY} without persisting a report file (RN-19.4).
 * </p>
 */
@RestController
@RequestMapping("/api/analytics/reports")
@RequiredArgsConstructor
@Tag(
        name = "Reports",
        description = """
                Manages asynchronous CSV report generation, instant row previews, download polling, \
                and report history for users with the `ADMINISTRADOR` role (RF-19). Report generation \
                follows an async request-poll pattern: submit a `POST` to queue the job (HTTP 202), \
                then poll `GET /{id}/download` until the status transitions from `PENDING` to `READY`. \
                A `preview` flag enables synchronous return of the first 10 rows without persisting \
                a file. Report history is limited to `READY` reports from the last 30 days.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final RequestReportUseCase requestReportUseCase;

    @PostMapping
    @Operation(
            operationId = "requestReport",
            summary = "Request CSV report generation or retrieve an instant 10-row preview",
            description = """
                    Submits a new analytics report generation request with the provided filter \
                    criteria (RF-19). The behaviour differs based on the `preview` field in the \
                    request body:

                    **Standard report (preview: false or absent):**
                    The report job is queued for asynchronous processing. The response is \
                    HTTP 202 with status `PENDING` and a report `id`. The client must poll \
                    `GET /api/analytics/reports/{id}/download` at intervals until the status \
                    changes to `READY` or `FAILED`. Full reports are persisted as files and \
                    made available via a signed download URL.

                    **Preview mode (preview: true):**
                    Returns the first 10 data rows synchronously as HTTP 200 with status `READY` \
                    and the preview file URL already populated. No report file is persisted — \
                    preview results are ephemeral (RN-19.4).

                    **Filter fields:**
                    - `dateFrom` / `dateTo` — mandatory date boundaries (ISO-8601 `yyyy-MM-dd`); \
                      can be overridden by the nested `dateRange` object if present
                    - `metrics` — subset of metric dimensions to include (`USERS`, `PARCHES`, \
                      `EVENTS`, `MATCHES`, `ZONES`); omit or set to `null` to include all
                    - `category` — optional parche category filter
                    - `campusZone` — optional campus zone filter
                    - `includeAdmin` — when `true`, includes administrative parches in the results
                    - `format` — export format; defaults to `CSV` when absent
                    - `schedule` — optional recurring delivery config; when set, registers a \
                      background job that regenerates and delivers the report at the configured \
                      frequency (RN-19.6)

                    **Identity:** the requester UUID is extracted from the JWT `sub` claim; \
                    no user ID field is needed in the request body.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Preview mode only (`preview: true`). The first 10 rows were computed \
                            synchronously and are available immediately. The `status` field in the \
                            response body is `READY` and `fileUrl` is populated. No file is \
                            persisted; this result is ephemeral (RN-19.4).
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReportRequestResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "202",
                    description = """
                            Standard report accepted and queued for asynchronous generation. \
                            The `status` field is `PENDING` and `fileUrl` is `null`. Poll \
                            `GET /api/analytics/reports/{id}/download` using the returned `id` \
                            until status changes to `READY` or `FAILED`.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReportRequestResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            The request body failed validation. Common causes: `dateFrom` or \
                            `dateTo` is missing, `dateFrom` is after `dateTo`, an unrecognised \
                            `metrics` value was provided, or `format` is not a valid `ExportFormat`. \
                            The response body contains field-level validation error details.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            No valid JWT Bearer token was provided or the token has expired. \
                            Re-authenticate and retry with a fresh token.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            The authenticated user does not have permission to generate reports. \
                            Report generation requires the `ADMINISTRADOR` role.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            An unexpected server error occurred while queuing the report job. \
                            Retry the request; if the problem persists, contact support.
                            """,
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ReportRequestResponse> requestReport(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            Filter criteria for the report. `dateFrom` and `dateTo` are required \
                            unless a `dateRange` object is provided (which takes precedence). \
                            Set `preview: true` for an instant 10-row preview without file persistence.
                            """,
                    required = true,
                    content = @Content(schema = @Schema(implementation = ReportFiltersRequest.class))
            )
            @Valid @RequestBody ReportFiltersRequest filtersRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {

        ReportRequest report = requestReportUseCase.create(userId, filtersRequest.toDomain());
        if (filtersRequest.isPreview()) {
            return ResponseEntity.ok(ReportRequestResponse.from(report));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ReportRequestResponse.from(report));
    }

    @GetMapping("/{id}/download")
    @Operation(
            operationId = "downloadReport",
            summary = "Poll for report readiness and retrieve the download URL when ready",
            description = """
                    Checks the current status of a previously requested report and, when the report \
                    is `READY`, returns the signed file URL for downloading the CSV (RF-19).

                    This endpoint is designed to be polled by the client after submitting a report \
                    request via `POST /api/analytics/reports`. The three possible outcomes are:

                    - **HTTP 200 — READY:** the report has been generated successfully. The response \
                      body contains the signed file URL. The client should initiate the file download \
                      immediately, as signed URLs may have a limited validity window.

                    - **HTTP 202 — PENDING:** the report is still being processed. The client should \
                      wait and retry after a short interval (recommended: 3–5 seconds between polls).

                    - **HTTP 500 — FAILED:** the report generation process encountered an unrecoverable \
                      error. The client should notify the user and offer the option to submit a new \
                      report request.

                    **Ownership check:** the `userId` extracted from the JWT must match the requester \
                    of the report identified by `{id}`. Accessing another user's report returns HTTP 403.

                    **Path parameter:** `{id}` must be the UUID returned in the `id` field of the \
                    `POST` response.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            The report is `READY`. The response body is the signed download URL \
                            for the generated CSV file. Initiate the download promptly — signed \
                            URLs may expire.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "https://storage.eci.edu.co/reports/abc123.csv?token=...")
                    )
            ),
            @ApiResponse(
                    responseCode = "202",
                    description = """
                            The report is still `PENDING` — generation is in progress. \
                            Retry this endpoint after 3–5 seconds. The response body contains \
                            a human-readable message: `"The report is still being generated. Try again in a few moments."`
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            No valid JWT Bearer token was provided or the token has expired. \
                            Re-authenticate before retrying.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            The report identified by `{id}` exists but belongs to a different user. \
                            Each user can only access their own reports.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            No report request was found for the provided `{id}` UUID. Verify that \
                            the ID was copied correctly from the `POST` response.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            The report status is `FAILED` — an unrecoverable error occurred during \
                            generation. The response body contains: \
                            `"Report generation failed. Try requesting it again."` \
                            Submit a new report request to retry.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string")
                    )
            )
    })
    public ResponseEntity<String> downloadReport(
            @Parameter(
                    description = "UUID of the report request to check, as returned by the POST endpoint.",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {

        ReportRequest report = requestReportUseCase.findById(id, userId);

        if (report.getStatus() == ReportStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("The report is still being generated. Try again in a few moments.");
        }
        if (report.getStatus() == ReportStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Report generation failed. Try requesting it again.");
        }
        return ResponseEntity.ok(report.getFileUrl());
    }

    @GetMapping("/history")
    @Operation(
            operationId = "getReportHistory",
            summary = "Retrieve the last 30 days of completed reports for the authenticated user",
            description = """
                    Returns the report history for the authenticated user, listing all successfully \
                    completed (`READY`) reports generated within the last 30 days (RF-19 RN-19.5). \
                    This endpoint is intended for the report history screen where users can \
                    re-download previously generated files.

                    **Filtering rules:**
                    - Only reports with status `READY` are included — `PENDING` and `FAILED` \
                      reports are excluded from the history list
                    - Only reports created within the last 30 calendar days are returned; \
                      older reports are automatically excluded
                    - Results are scoped to the authenticated user — reports from other users \
                      are never included

                    **Empty result:** if no completed reports exist within the 30-day window, \
                    the response is HTTP 200 with an empty array `[]` — never HTTP 404.

                    **Identity:** the user UUID is extracted from the JWT `sub` claim; \
                    no additional parameters are required.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Report history retrieved successfully. The array contains entries for \
                            all `READY` reports from the last 30 days. Returns an empty array `[]` \
                            if no reports match the criteria — never a 404.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReportHistoryDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            No valid JWT Bearer token was provided or the token has expired. \
                            Re-authenticate and retry.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            The authenticated user does not have the `ADMINISTRADOR` role required \
                            to access report history. Ensure the correct account and role are being used.
                            """,
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            An unexpected server error occurred while retrieving the report history. \
                            Retry the request or contact support if the issue persists.
                            """,
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<ReportHistoryDTO>> getReportHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(requestReportUseCase.getHistory(userId));
    }
}