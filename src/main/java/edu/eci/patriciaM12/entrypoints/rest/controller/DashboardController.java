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

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "dashboard personal del estudiante")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final GetStudentDashboardUseCase getStudentDashboardUseCase;

    @GetMapping("/dashboard")
    @Operation(summary = "Retorna las métricas personales del estudiante autenticado")
    public ResponseEntity<StudentDashboardResponse> getDashboard(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        StudentDashboardResponse response = StudentDashboardResponse
                .from(getStudentDashboardUseCase.execute(userId));
        return ResponseEntity.ok(response);
    }
}