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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
@Tag(name = "Admin Analytics", description = "Analytics panel for administrators")
@SecurityRequirement(name = "bearerAuth")
public class AdminAnalyticsController {

    private final GetAdminAnalyticsUseCase getAdminAnalyticsUseCase;

    /**
     * Returns the global analytics panel for administrators.
     * <p>
     * When {@code startDate} or {@code endDate} are omitted the active academic semester is used.
     * When {@code metricType} is omitted all metric categories are included in the response.
     * When {@code facultyFilter} is omitted no faculty restriction is applied (RN-18.10).
     * </p>
     *
     * @param authorization the Bearer token forwarded from the gateway (hidden from Swagger UI)
     * @param startDate     optional start of the query window (ISO date)
     * @param endDate       optional end of the query window (ISO date)
     * @param metricType    optional metric filter; {@code null} means all metrics
     * @param facultyFilter optional faculty name to restrict analytics scope; {@code null} means all faculties
     * @return HTTP 200 with the populated {@link AdminAnalyticsResponse}
     */
    @GetMapping
    @Operation(summary = "Returns the global analytics panel for administrators")
    public ResponseEntity<AdminAnalyticsResponse> getAdminAnalyticsPanel(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) MetricType metricType,
            @RequestParam(required = false) String facultyFilter) {
        return ResponseEntity.ok(
                getAdminAnalyticsUseCase.getPanel(startDate, endDate, metricType, facultyFilter));
    }
}
