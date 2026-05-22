package edu.eci.patriciaM12;

import edu.eci.patriciaM12.application.service.DashboardService;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.ParticipationLevel;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import edu.eci.patriciaM12.infrastructure.external.HangoutFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private StudentMetricsRepositoryPort studentMetricsRepository;
    @Mock private HangoutFeignClient hangoutFeignClient;

    private DashboardService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new DashboardService(studentMetricsRepository, hangoutFeignClient);
    }

    @Test
    void retornaMetricaExistenteCuandoRepositorioLaEncuentra() {
        StudentDashboardMetric metric = buildMetric(userId, 6, PatchCategory.GAMING);
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));
        when(hangoutFeignClient.getUserParcheCount(userId)).thenReturn(6);

        StudentDashboardMetric result = service.execute(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getPatchesAttended()).isEqualTo(6);
        assertThat(result.getTopCategory()).isEqualTo(PatchCategory.GAMING);
    }

    @Test
    void retornaSnapshotVacioCuandoNoExistenMetricas() {
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        StudentDashboardMetric result = service.execute(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getPatchesAttended()).isEqualTo(0);
        assertThat(result.getTopCategory()).isNull();
        assertThat(result.getPeriod()).isEqualTo(LocalDate.now());
        assertThat(result.getComputedAt()).isNotNull();
    }

    @Test
    void snapshotVacioTieneTodosLosDiasDeLaSemanaEnCero() {
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        StudentDashboardMetric result = service.execute(userId);

        assertThat(result.getWeeklyActivity()).hasSize(7);
        result.getWeeklyActivity().values()
                .forEach(v -> assertThat(v).isEqualTo(0));
    }

    @Test
    void retornaNivelNuevoCuandoTieneCeroParches() {
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThat(service.execute(userId).getParticipationLevel())
                .isEqualTo(ParticipationLevel.NUEVO);
    }

    @Test
    void retornaNivelActivoCuandoTieneTresParches() {
        StudentDashboardMetric metric = buildMetric(userId, 3, PatchCategory.SPORTS);
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));
        when(hangoutFeignClient.getUserParcheCount(userId)).thenReturn(3);

        assertThat(service.execute(userId).getParticipationLevel())
                .isEqualTo(ParticipationLevel.ACTIVO);
    }

    @Test
    void retornaNivelConectorCuandoTieneDiezParches() {
        StudentDashboardMetric metric = buildMetric(userId, 10, PatchCategory.STUDY);
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));
        when(hangoutFeignClient.getUserParcheCount(userId)).thenReturn(10);

        assertThat(service.execute(userId).getParticipationLevel())
                .isEqualTo(ParticipationLevel.CONECTOR);
    }

    @Test
    void retornaNivelEmbajadorCuandoTieneVeinteParches() {
        StudentDashboardMetric metric = buildMetric(userId, 20, PatchCategory.CULTURE);
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));
        when(hangoutFeignClient.getUserParcheCount(userId)).thenReturn(20);

        assertThat(service.execute(userId).getParticipationLevel())
                .isEqualTo(ParticipationLevel.EMBAJADOR);
    }

    @Test
    void isStaleRetornaFalseCuandoComputedAtEsReciente() {
        StudentDashboardMetric metric = buildMetricWithTime(userId, LocalDateTime.now());
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));

        assertThat(service.execute(userId).isStale()).isFalse();
    }

    @Test
    void isStaleRetornaTrueCuandoDesfaseSuperaCincoMinutos() {
        StudentDashboardMetric metric = buildMetricWithTime(userId, LocalDateTime.now().minusMinutes(10));
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));

        assertThat(service.execute(userId).isStale()).isTrue();
    }

    private StudentDashboardMetric buildMetric(UUID userId, int patches, PatchCategory category) {
        Map<DayOfWeek, Integer> weekly = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) weekly.put(d, 0);

        return StudentDashboardMetric.builder()
                .userId(userId)
                .period(LocalDate.now())
                .patchesAttended(patches)
                .topCategory(category)
                .weeklyActivity(weekly)
                .computedAt(LocalDateTime.now())
                .build();
    }

    private StudentDashboardMetric buildMetricWithTime(UUID userId, LocalDateTime computedAt) {
        Map<DayOfWeek, Integer> weekly = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) weekly.put(d, 0);

        return StudentDashboardMetric.builder()
                .userId(userId)
                .period(LocalDate.now())
                .patchesAttended(5)
                .topCategory(PatchCategory.FOOD)
                .weeklyActivity(weekly)
                .computedAt(computedAt)
                .build();
    }
}