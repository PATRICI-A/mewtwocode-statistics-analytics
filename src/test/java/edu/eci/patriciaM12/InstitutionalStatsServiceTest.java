package edu.eci.patriciaM12;

import edu.eci.patriciaM12.application.dto.response.InstitutionalStatsResponse;
import edu.eci.patriciaM12.application.service.InstitutionalStatsService;
import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.enums.InstitutionalMetricType;
import edu.eci.patriciaM12.domain.model.enums.ParticipationTrend;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstitutionalStatsServiceTest {

    @Mock private AdminSnapshotRepositoryPort adminSnapshotRepository;

    private InstitutionalStatsService service;

    @BeforeEach
    void setUp() {
        service = new InstitutionalStatsService(adminSnapshotRepository);
    }

    @Test
    void retornaTodosLosCamposCuandoMetricTypeEsAll() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusDays(10);
        when(adminSnapshotRepository.findByDateRange(any(), any()))
                .thenReturn(List.of(buildSnapshot(start, 50, 10)));

        InstitutionalStatsResponse response = service.execute(start, end, InstitutionalMetricType.ALL);

        assertThat(response.getEventsStats()).isNotNull();
        assertThat(response.getParticipationStats()).isNotNull();
        assertThat(response.getSocialActivityIndex()).isNotNull();
    }

    @Test
    void retornaSoloEventosCuandoMetricTypeEsEvents() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusDays(10);
        when(adminSnapshotRepository.findByDateRange(any(), any()))
                .thenReturn(List.of(buildSnapshot(start, 30, 5)));

        InstitutionalStatsResponse response = service.execute(start, end, InstitutionalMetricType.EVENTS);

        assertThat(response.getEventsStats()).isNotNull();
        assertThat(response.getEventsStats().getTotalCreated()).isEqualTo(5);
        assertThat(response.getParticipationStats()).isNull();
        assertThat(response.getSocialActivityIndex()).isNull();
    }

    @Test
    void retornaSoloParticipacionCuandoMetricTypeEsParticipation() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusDays(10);
        when(adminSnapshotRepository.findByDateRange(any(), any()))
                .thenReturn(List.of(buildSnapshot(start, 100, 20)));

        InstitutionalStatsResponse response = service.execute(start, end, InstitutionalMetricType.PARTICIPATION);

        assertThat(response.getParticipationStats()).isNotNull();
        assertThat(response.getParticipationStats().getTotalActiveStudents()).isEqualTo(100);
        assertThat(response.getEventsStats()).isNull();
        assertThat(response.getSocialActivityIndex()).isNull();
    }

    @Test
    void indiceActividadSocialNormalizadoEsMenorOIgualUno() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusDays(10);
        when(adminSnapshotRepository.findByDateRange(any(), any()))
                .thenReturn(List.of(buildSnapshot(start, 999999, 999999)));

        InstitutionalStatsResponse response = service.execute(start, end, InstitutionalMetricType.SOCIAL_ACTIVITY);

        assertThat(response.getSocialActivityIndex()).isLessThanOrEqualTo(1.0);
        assertThat(response.getSocialActivityIndex()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void detectaTendenciaCrecienteCorrectamente() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusDays(5);
        AdminAnalyticsSnapshot first = buildSnapshot(start, 10, 5);
        AdminAnalyticsSnapshot last = buildSnapshot(start.plusDays(5), 20, 5);
        when(adminSnapshotRepository.findByDateRange(any(), any()))
                .thenReturn(List.of(first, last));

        InstitutionalStatsResponse response = service.execute(start, end, InstitutionalMetricType.PARTICIPATION);

        assertThat(response.getParticipationStats().getParticipationTrend()).isEqualTo(ParticipationTrend.GROWING);
    }

    @Test
    void detectaTendenciaDecrecienteCorrectamente() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusDays(5);
        AdminAnalyticsSnapshot first = buildSnapshot(start, 100, 5);
        AdminAnalyticsSnapshot last = buildSnapshot(start.plusDays(5), 10, 5);
        when(adminSnapshotRepository.findByDateRange(any(), any()))
                .thenReturn(List.of(first, last));

        InstitutionalStatsResponse response = service.execute(start, end, InstitutionalMetricType.PARTICIPATION);

        assertThat(response.getParticipationStats().getParticipationTrend()).isEqualTo(ParticipationTrend.DECLINING);
    }

    @Test
    void usaSemestreActualCuandoFechasNulas() {
        when(adminSnapshotRepository.findByDateRange(any(), any())).thenReturn(List.of());

        InstitutionalStatsResponse response = service.execute(null, null, InstitutionalMetricType.ALL);

        assertThat(response).isNotNull();
        assertThat(response.getSocialActivityIndex()).isEqualTo(0.0);
    }

    private AdminAnalyticsSnapshot buildSnapshot(LocalDate date, int activeUsers, int totalPatches) {
        return AdminAnalyticsSnapshot.builder()
                .id(UUID.randomUUID())
                .snapshotDate(date)
                .activeUsers(activeUsers)
                .totalPatches(totalPatches)
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
