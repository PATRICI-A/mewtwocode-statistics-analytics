package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
import edu.eci.patriciaM12.domain.model.enums.MetricType;
import edu.eci.patriciaM12.domain.ports.in.GetAdminAnalyticsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Analytics", description = "Panel de analitica para administrador")
@SecurityRequirement(name = "bearerAuth")
public class AdminAnalyticsController {

    private final GetAdminAnalyticsUseCase getAdminAnalyticsUseCase;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Retorna el panel de analitica global del administrador")
    public ResponseEntity<AdminAnalyticsResponse> getAdminAnalyticsPanel(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) MetricType metricType,
            @AuthenticationPrincipal Jwt jwt) {
        UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(getAdminAnalyticsUseCase.getPanel(startDate, endDate, metricType));
    }
}
