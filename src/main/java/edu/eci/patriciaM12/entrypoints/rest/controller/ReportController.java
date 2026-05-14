package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.request.ReportFiltersRequest;
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

import java.util.UUID;

/**
 * REST controller that manages CSV report generation and download.
 * Report generation is asynchronous: a {@code POST} immediately returns HTTP 202 with a
 * {@code PENDING} status, and clients poll the {@code GET /{id}/download} endpoint until
 * the status transitions to {@code READY} or {@code FAILED}.
 */
@RestController
@RequestMapping("/api/analytics/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "CSV report exports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final RequestReportUseCase requestReportUseCase;

    /**
     * Accepts a CSV report generation request and queues it for asynchronous processing.
     *
     * @param filtersRequest the validated filter criteria for the report
     * @param jwt            the JWT token of the authenticated user; its subject is used as the requester ID
     * @return HTTP 202 Accepted with a {@link ReportRequestResponse} containing the report ID and
     *         initial {@code PENDING} status
     */
    @PostMapping
    @Operation(summary = "Requests CSV report generation")
    public ResponseEntity<ReportRequestResponse> requestReport(
            @Valid @RequestBody ReportFiltersRequest filtersRequest,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ReportRequest report = requestReportUseCase.create(userId, filtersRequest.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ReportRequestResponse.from(report));
    }

    /**
     * Returns the file URL for a completed CSV report.
     * Responds with HTTP 202 while the report is still {@code PENDING}, HTTP 500 if it
     * {@code FAILED}, and HTTP 200 with the file URL when it is {@code READY}.
     *
     * @param id  the UUID of the report request to check
     * @param jwt the JWT token of the authenticated user; ownership of the report is verified
     * @return HTTP 200 with the CSV file URL, HTTP 202 if still pending, or HTTP 500 on failure
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
}
