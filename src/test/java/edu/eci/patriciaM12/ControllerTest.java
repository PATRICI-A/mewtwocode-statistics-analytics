package edu.eci.patriciaM12;

import edu.eci.patriciaM12.application.dto.request.ReportFiltersRequest;
import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
import edu.eci.patriciaM12.application.dto.response.ReportRequestResponse;
import edu.eci.patriciaM12.application.dto.response.StudentDashboardResponse;
import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.MetricType;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import edu.eci.patriciaM12.domain.ports.in.GetAdminAnalyticsUseCase;
import edu.eci.patriciaM12.domain.ports.in.GetInteractionAnalyticsUseCase;
import edu.eci.patriciaM12.domain.ports.in.GetSocialIndicatorsUseCase;
import edu.eci.patriciaM12.domain.ports.in.GetStudentDashboardUseCase;
import edu.eci.patriciaM12.domain.ports.in.RequestReportUseCase;
import edu.eci.patriciaM12.entrypoints.rest.controller.AdminAnalyticsController;
import edu.eci.patriciaM12.entrypoints.rest.controller.DashboardController;
import edu.eci.patriciaM12.entrypoints.rest.controller.ReportController;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerTest {

    @Test
    void dashboardControllerRetornaMetricasDelUsuarioAutenticado() {
        GetStudentDashboardUseCase useCase = mock(GetStudentDashboardUseCase.class);
        GetSocialIndicatorsUseCase socialUseCase = mock(GetSocialIndicatorsUseCase.class);
        GetInteractionAnalyticsUseCase interactionUseCase = mock(GetInteractionAnalyticsUseCase.class);
        DashboardController controller = new DashboardController(useCase, socialUseCase, interactionUseCase);
        UUID userId = UUID.randomUUID();
        Jwt jwt = jwtFor(userId);
        Map<DayOfWeek, Integer> weekly = new EnumMap<>(DayOfWeek.class);
        weekly.put(DayOfWeek.MONDAY, 4);
        StudentDashboardMetric metric = StudentDashboardMetric.builder()
                .userId(userId)
                .patchesAttended(4)
                .topCategory(PatchCategory.CULTURE)
                .weeklyActivity(weekly)
                .computedAt(LocalDateTime.now())
                .build();
        when(useCase.execute(userId)).thenReturn(metric);

        ResponseEntity<StudentDashboardResponse> response = controller.getDashboard(jwt);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo(userId);
        assertThat(response.getBody().getTopCategory()).isEqualTo(PatchCategory.CULTURE);
    }

    @Test
    void reportControllerCreaReporteConEstadoAccepted() {
        RequestReportUseCase useCase = mock(RequestReportUseCase.class);
        ReportController controller = new ReportController(useCase);
        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        ReportFiltersRequest request = new ReportFiltersRequest();
        request.setDateFrom(LocalDate.of(2026, 1, 1));
        request.setDateTo(LocalDate.of(2026, 1, 2));
        ReportRequest report = ReportRequest.builder()
                .id(reportId)
                .requestedBy(userId)
                .status(ReportStatus.PENDING)
                .build();
        when(useCase.create(eq(userId), any(ReportFilters.class))).thenReturn(report);

        ResponseEntity<ReportRequestResponse> response = controller.requestReport(request, jwtFor(userId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(reportId);
        assertThat(response.getBody().getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void reportControllerDownloadRetornaAcceptedCuandoSiguePendiente() {
        RequestReportUseCase useCase = mock(RequestReportUseCase.class);
        ReportController controller = new ReportController(useCase);
        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        when(useCase.findById(reportId, userId)).thenReturn(ReportRequest.builder()
                .id(reportId)
                .requestedBy(userId)
                .status(ReportStatus.PENDING)
                .build());

        ResponseEntity<String> response = controller.downloadReport(reportId, jwtFor(userId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).contains("still being generated");
    }

    @Test
    void reportControllerDownloadRetornaErrorCuandoFalla() {
        RequestReportUseCase useCase = mock(RequestReportUseCase.class);
        ReportController controller = new ReportController(useCase);
        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        when(useCase.findById(reportId, userId)).thenReturn(ReportRequest.builder()
                .id(reportId)
                .requestedBy(userId)
                .status(ReportStatus.FAILED)
                .build());

        ResponseEntity<String> response = controller.downloadReport(reportId, jwtFor(userId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("failed");
    }

    @Test
    void reportControllerDownloadRetornaUrlCuandoEstaReady() {
        RequestReportUseCase useCase = mock(RequestReportUseCase.class);
        ReportController controller = new ReportController(useCase);
        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        when(useCase.findById(reportId, userId)).thenReturn(ReportRequest.builder()
                .id(reportId)
                .requestedBy(userId)
                .status(ReportStatus.READY)
                .fileUrl("/tmp/report.csv")
                .build());

        ResponseEntity<String> response = controller.downloadReport(reportId, jwtFor(userId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("/tmp/report.csv");
    }

    @Test
    void adminAnalyticsControllerDelegaFiltrosAlCasoDeUso() {
        GetAdminAnalyticsUseCase useCase = mock(GetAdminAnalyticsUseCase.class);
        AdminAnalyticsController controller = new AdminAnalyticsController(useCase);
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        AdminAnalyticsResponse expected = AdminAnalyticsResponse.builder()
                .matchSuccessRate(0.4)
                .build();
        when(useCase.getPanel(start, end, MetricType.MATCHES, null)).thenReturn(expected);

        ResponseEntity<AdminAnalyticsResponse> response =
                controller.getAdminAnalyticsPanel("Bearer token", start, end, MetricType.MATCHES, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
    }

    private Jwt jwtFor(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId.toString())
                .build();
    }
}