package edu.eci.patriciaM12.entrypoints.rest.controller;

import edu.eci.patriciaM12.application.dto.response.StudentDashboardResponse;
import edu.eci.patriciaM12.domain.ports.in.GetStudentDashboardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller that exposes the student personal dashboard endpoint.
 * The student identity is resolved from the JWT principal, so no explicit user ID is
 * required in the request.  The endpoint always returns HTTP 200 (Empty Object Pattern).
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Personal student dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final GetStudentDashboardUseCase getStudentDashboardUseCase;

    /**
     * Returns the personal activity metrics for the authenticated student.
     * If no metrics have been persisted yet a synthetic zeroed-out response is returned
     * (HTTP 200, never 404).
     *
     * @param jwt the JWT token injected by Spring Security; the subject claim is used as the user ID
     * @return HTTP 200 with the student's {@link StudentDashboardResponse}
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Returns personal metrics for the authenticated student")
    public ResponseEntity<StudentDashboardResponse> getDashboard(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        StudentDashboardResponse response = StudentDashboardResponse
                .from(getStudentDashboardUseCase.execute(userId));
        return ResponseEntity.ok(response);
    }
}
