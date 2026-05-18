package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.request.ReportFiltersRequest;
import edu.eci.patriciaM12.application.dto.response.ReportHistoryDTO;
import edu.eci.patriciaM12.application.dto.response.ReportRequestResponse;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import edu.eci.patriciaM12.domain.ports.in.RequestReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@Tag(name = "Reports", description = "CSV report exports and report history")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final RequestReportUseCase requestReportUseCase;

    /**
     * Accepts a CSV report generation request and queues it for asynchronous processing.
     * <p>
     * When {@code preview} is {@code true} in the request body the response is HTTP 200 with
     * status {@code READY} and the preview file URL populated immediately (RN-19.4).
     * Otherwise the response is HTTP 202 with status {@code PENDING}.
     * </p>
     *
     * @param filtersRequest the validated filter criteria for the report
     * @param jwt            the JWT token of the authenticated user; its subject is used as the requester ID
     * @return HTTP 200 (preview) or HTTP 202 (full report) with a {@link ReportRequestResponse}
     */
    @PostMapping
    @Operation(summary = "Requests CSV report generation or returns a 10-row preview")
    public ResponseEntity<ReportRequestResponse> requestReport(
            @Valid @RequestBody ReportFiltersRequest filtersRequest,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ReportRequest report = requestReportUseCase.create(userId, filtersRequest.toDomain());
        if (filtersRequest.isPreview()) {
            return ResponseEntity.ok(ReportRequestResponse.from(report));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ReportRequestResponse.from(report));
    }

    /**
     * Returns the file URL for a completed CSV report.
     * <ul>
     *   <li>HTTP 200 — report is {@code READY}, body contains the file URL</li>
     *   <li>HTTP 202 — report is still {@code PENDING}, try again later</li>
     *   <li>HTTP 500 — report generation {@code FAILED}</li>
     * </ul>
     *
     * @param id  the UUID of the report request to check
     * @param jwt the JWT token of the authenticated user; ownership of the report is verified
     * @return the appropriate HTTP response
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "Downloads the CSV when the report is READY")
    public ResponseEntity<String> downloadReport(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
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

    /**
     * Returns the report history for the authenticated user (RF-19 RN-19.5).
     * Only {@code READY} reports created in the last 30 days are included.
     *
     * @param jwt the JWT token of the authenticated user
     * @return HTTP 200 with the list of {@link ReportHistoryDTO} entries; may be empty
     */
    @GetMapping("/history")
    @Operation(summary = "Returns the last 30 days of completed reports for the authenticated user")
    public ResponseEntity<List<ReportHistoryDTO>> getReportHistory(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(requestReportUseCase.getHistory(userId));
    }
}
