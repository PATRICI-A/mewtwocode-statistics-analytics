package edu.eci.patriciaM12;

import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
import edu.eci.patriciaM12.application.service.AdminAnalyticsService;
import edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException;
import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.enums.MetricType;
import edu.eci.patriciaM12.domain.ports.out.AdminSnapshotRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceTest {

    @Mock
    private AdminSnapshotRepositoryPort adminSnapshotRepository;

    private AdminAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AdminAnalyticsService(adminSnapshotRepository);
    }

    @Test
    void retornaTodasLasMetricasCuandoMetricTypeEsNull() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = startDate.plusDays(1);
        AdminAnalyticsSnapshot snapshot = buildSnapshot(startDate, 12, 4);
        when(adminSnapshotRepository.findByDateRange(any(), any())).thenReturn(List.of());
        when(adminSnapshotRepository.findByDateRange(startDate, endDate)).thenReturn(List.of(snapshot));

        AdminAnalyticsResponse response = service.getPanel(startDate, endDate, null, null);

        assertThat(response.getActiveUsers().getTimeSeries()).containsEntry(startDate, 12);
        assertThat(response.getParcheStats().getTotal()).isEqualTo(4);
        assertThat(response.getTopEvents()).isEmpty();
        assertThat(response.getMatchSuccessRate()).isZero();
        assertThat(response.getCampusHeatmap()).isNull();
    }

    @Test
    void retornaSoloUsuariosCuandoMetricTypeEsUsers() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = startDate.plusDays(1);
        AdminAnalyticsSnapshot snapshot = buildSnapshot(startDate, 8, 2);
        when(adminSnapshotRepository.findByDateRange(any(), any())).thenReturn(List.of());
        when(adminSnapshotRepository.findByDateRange(startDate, endDate)).thenReturn(List.of(snapshot));

        AdminAnalyticsResponse response = service.getPanel(startDate, endDate, MetricType.USERS, null);

        assertThat(response.getActiveUsers()).isNotNull();
        assertThat(response.getParcheStats()).isNull();
        assertThat(response.getTopEvents()).isNull();
        assertThat(response.getMatchSuccessRate()).isNull();
        assertThat(response.getCampusHeatmap()).isNull();
    }

    @Test
    void rechazaEndDateCuandoNoEsPosteriorAStartDate() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);

        assertThatThrownBy(() -> service.getPanel(startDate, startDate, null, null))
                .isInstanceOf(InvalidReportFiltersException.class)
                .hasMessage("endDate must be after startDate.");
    }

    private AdminAnalyticsSnapshot buildSnapshot(LocalDate snapshotDate, int activeUsers, int totalPatches) {
        return AdminAnalyticsSnapshot.builder()
                .id(UUID.randomUUID())
                .snapshotDate(snapshotDate)
                .activeUsers(activeUsers)
                .totalPatches(totalPatches)
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
