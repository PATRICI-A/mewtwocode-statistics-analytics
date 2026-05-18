package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.response.InstitutionalStatsResponse;
import edu.eci.patriciaM12.domain.model.enums.InstitutionalMetricType;
import edu.eci.patriciaM12.domain.ports.in.GetInstitutionalStatsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
 * In the {@code dev} Spring profile the security filter chain is permissive for
 * {@code /api/v1/analytics/admin/**} paths; the institutional endpoint uses the path
 * {@code /api/v1/analytics/institutional} and is therefore always protected.</p>
 */
@RestController
@RequestMapping("/api/v1/analytics/institutional")
@RequiredArgsConstructor
@Tag(name = "Institutional Statistics", description = "Campus-wide statistics for Bienestar users")
@SecurityRequirement(name = "bearerAuth")
public class InstitutionalStatsController {

    private final GetInstitutionalStatsUseCase getInstitutionalStatsUseCase;

    /**
     * Returns institutional statistics for the requested date range and metric filter.
     * <p>
     * When {@code startDate} or {@code endDate} are absent the active academic semester boundaries
     * are used.  When {@code metricType} is absent, {@link InstitutionalMetricType#ALL} is assumed.
     * </p>
     *
     * @param authorization the Bearer token forwarded from the gateway (hidden from Swagger UI)
     * @param startDate     optional inclusive start of the observed period (ISO date)
     * @param endDate       optional inclusive end of the observed period (ISO date)
     * @param metricType    optional category of metrics to include; defaults to {@code ALL}
     * @return HTTP 200 with the computed {@link InstitutionalStatsResponse}
     */
    @GetMapping
    @Operation(summary = "Returns institutional statistics for Bienestar users (RF-40)")
    public ResponseEntity<InstitutionalStatsResponse> getInstitutionalStats(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "ALL") InstitutionalMetricType metricType) {
        return ResponseEntity.ok(
                getInstitutionalStatsUseCase.execute(startDate, endDate, metricType));
    }
}
