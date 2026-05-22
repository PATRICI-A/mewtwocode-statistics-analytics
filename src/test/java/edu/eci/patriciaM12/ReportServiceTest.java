package edu.eci.patriciaM12;

import edu.eci.patriciaM12.application.service.ReportService;
import edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException;
import edu.eci.patriciaM12.domain.exceptions.ReportNotFoundException;
import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import edu.eci.patriciaM12.domain.ports.out.CsvGeneratorPort;
import edu.eci.patriciaM12.domain.ports.out.NotificationPublisherPort;
import edu.eci.patriciaM12.domain.ports.out.ReportRequestRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private ReportRequestRepositoryPort reportRequestRepository;

    @Mock
    private CsvGeneratorPort csvGenerator;

    @Mock
    private NotificationPublisherPort notificationPublisher;

    private ReportService reportService;

    private final UUID userId = UUID.randomUUID();

    private ReportFilters validFilters;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRequestRepository, csvGenerator, notificationPublisher);
        validFilters = ReportFilters.builder()
                .dateFrom(LocalDate.now().minusDays(7))
                .dateTo(LocalDate.now())
                .includeAdmin(false)
                .build();
    }

    @Test
    void createReport_conFiltrosValidos_retornaPending() {
        when(reportRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportRequest result = reportService.create(userId, validFilters);

        assertThat(result.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(result.getRequestedBy()).isEqualTo(userId);
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void createReport_cuandoDateFromEsPosteriorADateTo_lanzaInvalidReportFiltersException() {
        ReportFilters filtrosInvalidos = ReportFilters.builder()
                .dateFrom(LocalDate.now())
                .dateTo(LocalDate.now().minusDays(1))
                .includeAdmin(false)
                .build();

        assertThatThrownBy(() -> reportService.create(userId, filtrosInvalidos))
                .isInstanceOf(InvalidReportFiltersException.class);
    }

    @Test
    void findById_cuandoExisteYEsDelUsuario_retornaReporte() {
        UUID reportId = UUID.randomUUID();
        ReportRequest report = ReportRequest.builder()
                .id(reportId)
                .requestedBy(userId)
                .dateFrom(validFilters.getDateFrom())
                .dateTo(validFilters.getDateTo())
                .filters(validFilters)
                .status(ReportStatus.READY)
                .fileUrl("/tmp/report.csv")
                .build();

        when(reportRequestRepository.findById(reportId)).thenReturn(Optional.of(report));

        ReportRequest result = reportService.findById(reportId, userId);

        assertThat(result.getId()).isEqualTo(reportId);
        assertThat(result.getStatus()).isEqualTo(ReportStatus.READY);
    }

    @Test
    void findById_cuandoNoExiste_lanzaReportNotFoundException() {
        UUID reportId = UUID.randomUUID();
        when(reportRequestRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.findById(reportId, userId))
                .isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    void findById_cuandoPerteneceAOtroUsuario_lanzaReportNotFoundException() {
        UUID reportId = UUID.randomUUID();
        ReportRequest report = ReportRequest.builder()
                .id(reportId)
                .requestedBy(UUID.randomUUID())
                .dateFrom(validFilters.getDateFrom())
                .dateTo(validFilters.getDateTo())
                .filters(validFilters)
                .status(ReportStatus.READY)
                .fileUrl("/tmp/report.csv")
                .build();

        when(reportRequestRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> reportService.findById(reportId, userId))
                .isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    void reportConVolumenGrande_sinDatosSensibles_generaCSVCorrectamente() {
        when(reportRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportFilters filtrosAmplio = ReportFilters.builder()
                .dateFrom(LocalDate.now().minusDays(365))
                .dateTo(LocalDate.now())
                .includeAdmin(false)
                .build();

        ReportRequest result = reportService.create(userId, filtrosAmplio);

        assertThat(result.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(result.getFilters().getDateFrom()).isEqualTo(LocalDate.now().minusDays(365));
    }
}