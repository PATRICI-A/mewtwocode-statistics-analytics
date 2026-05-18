package edu.eci.patriciaM12;

import edu.eci.patriciaM12.application.service.MetricEventProcessorService;
import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.MetricEvent;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.MetricEventType;
import edu.eci.patriciaM12.domain.ports.out.AdminSnapshotRepositoryPort;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricEventProcessorServiceTest {

    @Mock private StudentMetricsRepositoryPort studentMetricsRepository;
    @Mock private AdminSnapshotRepositoryPort adminSnapshotRepository;

    private MetricEventProcessorService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID patchId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new MetricEventProcessorService(studentMetricsRepository, adminSnapshotRepository);
    }

    @Test
    void procesarJoinIncrementaPatchesAtendidosYActiveUsers() {
        StudentDashboardMetric existingMetric = buildMetric(3);
        AdminAnalyticsSnapshot snapshot = buildSnapshot(5, 10);
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(existingMetric));
        when(adminSnapshotRepository.findByDate(any())).thenReturn(Optional.of(snapshot));

        service.process(joinEvent());

        ArgumentCaptor<StudentDashboardMetric> metricCaptor = ArgumentCaptor.forClass(StudentDashboardMetric.class);
        verify(studentMetricsRepository).save(metricCaptor.capture());
        assertThat(metricCaptor.getValue().getPatchesAttended()).isEqualTo(4);

        ArgumentCaptor<AdminAnalyticsSnapshot> snapshotCaptor = ArgumentCaptor.forClass(AdminAnalyticsSnapshot.class);
        verify(adminSnapshotRepository).save(snapshotCaptor.capture());
        assertThat(snapshotCaptor.getValue().getActiveUsers()).isEqualTo(6);
        assertThat(snapshotCaptor.getValue().getTotalPatches()).isEqualTo(10);
    }

    @Test
    void procesarJoinCreaMetricaVaciaCuandoNoExiste() {
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(adminSnapshotRepository.findByDate(any())).thenReturn(Optional.empty());

        service.process(joinEvent());

        ArgumentCaptor<StudentDashboardMetric> captor = ArgumentCaptor.forClass(StudentDashboardMetric.class);
        verify(studentMetricsRepository).save(captor.capture());
        assertThat(captor.getValue().getPatchesAttended()).isEqualTo(1);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    void procesarJoinActualizaWeeklyActivity() {
        Map<DayOfWeek, Integer> weekly = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) weekly.put(d, 0);
        StudentDashboardMetric metric = StudentDashboardMetric.builder()
                .userId(userId)
                .patchesAttended(2)
                .weeklyActivity(weekly)
                .computedAt(LocalDateTime.now())
                .build();
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));
        when(adminSnapshotRepository.findByDate(any())).thenReturn(Optional.empty());

        service.process(joinEvent());

        ArgumentCaptor<StudentDashboardMetric> captor = ArgumentCaptor.forClass(StudentDashboardMetric.class);
        verify(studentMetricsRepository).save(captor.capture());
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        assertThat(captor.getValue().getWeeklyActivity().get(today)).isEqualTo(1);
    }

    @Test
    void procesarCreateIncrementaTotalPatches() {
        AdminAnalyticsSnapshot snapshot = buildSnapshot(5, 10);
        when(adminSnapshotRepository.findByDate(any())).thenReturn(Optional.of(snapshot));

        service.process(createEvent());

        ArgumentCaptor<AdminAnalyticsSnapshot> captor = ArgumentCaptor.forClass(AdminAnalyticsSnapshot.class);
        verify(adminSnapshotRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalPatches()).isEqualTo(11);
        assertThat(captor.getValue().getActiveUsers()).isEqualTo(5);
        verify(studentMetricsRepository, never()).save(any());
    }

    @Test
    void procesarViewNoTocaRepositorios() {
        service.process(eventOf(MetricEventType.VIEW));

        verify(studentMetricsRepository, never()).save(any());
        verify(adminSnapshotRepository, never()).save(any());
    }

    @Test
    void procesarLeaveNoTocaRepositorios() {
        service.process(eventOf(MetricEventType.LEAVE));

        verify(studentMetricsRepository, never()).save(any());
        verify(adminSnapshotRepository, never()).save(any());
    }

    @Test
    void procesarDeleteNoTocaRepositorios() {
        service.process(eventOf(MetricEventType.DELETE));

        verify(studentMetricsRepository, never()).save(any());
        verify(adminSnapshotRepository, never()).save(any());
    }

    @Test
    void eventoNullEsIgnorado() {
        service.process(null);

        verify(studentMetricsRepository, never()).save(any());
        verify(adminSnapshotRepository, never()).save(any());
    }

    @Test
    void eventoSinTipoEsIgnorado() {
        MetricEvent event = MetricEvent.builder()
                .eventId(UUID.randomUUID())
                .payload(Map.of("userId", userId.toString()))
                .emittedAt(LocalDateTime.now())
                .build();

        service.process(event);

        verify(studentMetricsRepository, never()).save(any());
        verify(adminSnapshotRepository, never()).save(any());
    }

    @Test
    void joinConUserIdInvalidoEsIgnorado() {
        MetricEvent event = MetricEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(MetricEventType.JOIN)
                .payload(Map.of("userId", "not-a-uuid"))
                .emittedAt(LocalDateTime.now())
                .build();

        service.process(event);

        verify(studentMetricsRepository, never()).save(any());
    }

    @Test
    void joinSinUserIdEsIgnorado() {
        MetricEvent event = MetricEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(MetricEventType.JOIN)
                .payload(Map.of())
                .emittedAt(LocalDateTime.now())
                .build();

        service.process(event);

        verify(studentMetricsRepository, never()).save(any());
    }

    private MetricEvent joinEvent() {
        return MetricEvent.builder()
                .eventId(UUID.randomUUID())
                .sourceModule("M06")
                .eventType(MetricEventType.JOIN)
                .payload(Map.of("userId", userId.toString(), "patchId", patchId.toString()))
                .emittedAt(LocalDateTime.now())
                .build();
    }

    private MetricEvent createEvent() {
        return MetricEvent.builder()
                .eventId(UUID.randomUUID())
                .sourceModule("M06")
                .eventType(MetricEventType.CREATE)
                .payload(Map.of("patchId", patchId.toString()))
                .emittedAt(LocalDateTime.now())
                .build();
    }

    private MetricEvent eventOf(MetricEventType type) {
        return MetricEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(type)
                .payload(Map.of())
                .emittedAt(LocalDateTime.now())
                .build();
    }

    private StudentDashboardMetric buildMetric(int patches) {
        Map<DayOfWeek, Integer> weekly = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) weekly.put(d, 0);
        return StudentDashboardMetric.builder()
                .userId(userId)
                .period(LocalDate.now())
                .patchesAttended(patches)
                .weeklyActivity(weekly)
                .computedAt(LocalDateTime.now())
                .build();
    }

    private AdminAnalyticsSnapshot buildSnapshot(int activeUsers, int totalPatches) {
        return AdminAnalyticsSnapshot.builder()
                .id(UUID.randomUUID())
                .snapshotDate(LocalDate.now())
                .activeUsers(activeUsers)
                .totalPatches(totalPatches)
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
