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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Exportación de reportes CSV")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final RequestReportUseCase requestReportUseCase;

    @PostMapping
    @Operation(summary = "Solicitar generación de reporte CSV")
    public ResponseEntity<ReportRequestResponse> requestReport(
            @Valid @RequestBody ReportFiltersRequest filtersRequest,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ReportRequest report = requestReportUseCase.create(userId, filtersRequest.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ReportRequestResponse.from(report));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Descargar CSV cuando el reporte está READY")
    public ResponseEntity<String> downloadReport(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ReportRequest report = requestReportUseCase.findById(id, userId);

        if (report.getStatus() == ReportStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("El reporte aún está siendo generado. Intente de nuevo en unos momentos.");
        }
        if (report.getStatus() == ReportStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("La generación del reporte falló. Intente solicitarlo de nuevo.");
        }
        return ResponseEntity.ok(report.getFileUrl());
    }
}