package edu.eci.patriciaM12;

import edu.eci.patriciaM12.application.dto.response.InteractionAnalyticsResponse;
import edu.eci.patriciaM12.application.service.InteractionAnalyticsService;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import edu.eci.patriciaM12.infrastructure.external.GeolocationFeignClient;
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
class InteractionAnalyticsServiceTest {

    @Mock private StudentMetricsRepositoryPort studentMetricsRepository;
    @Mock private GeolocationFeignClient       geolocationFeignClient;

    private InteractionAnalyticsService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new InteractionAnalyticsService(studentMetricsRepository, geolocationFeignClient);
    }

    @Test
    void retornaContadoresVaciosCuandoNoHayMetricas() {
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        InteractionAnalyticsResponse response = service.execute(userId);

        assertThat(response).isNotNull();
        assertThat(response.getTotalInteractions()).isZero();
        assertThat(response.getMostActiveZone()).isNull();
        assertThat(response.getPeakActivityDay()).isNull();
        assertThat(response.getInteractionSummary()).containsEntry("parcheAttendance", 0);
        assertThat(response.getInteractionSummary()).containsEntry("rsvpConfirmed", 0);
        assertThat(response.getInteractionSummary()).containsEntry("connectionRequest", 0);
    }

    @Test
    void sumaTotalCorrectamenteCuandoHayMetricas() {
        Map<DayOfWeek, Integer> weekly = new EnumMap<>(DayOfWeek.class);
        weekly.put(DayOfWeek.MONDAY, 3);
        weekly.put(DayOfWeek.TUESDAY, 2);
        StudentDashboardMetric metric = buildMetric(5, weekly);
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));

        InteractionAnalyticsResponse response = service.execute(userId);

        assertThat(response.getInteractionSummary().get("parcheAttendance")).isEqualTo(5);
        assertThat(response.getInteractionSummary().get("rsvpConfirmed")).isEqualTo(5);
        assertThat(response.getTotalInteractions()).isEqualTo(10);
    }

    @Test
    void detectaDiaPicoCorrectamente() {
        Map<DayOfWeek, Integer> weekly = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) weekly.put(d, 0);
        weekly.put(DayOfWeek.WEDNESDAY, 7);
        weekly.put(DayOfWeek.FRIDAY, 3);
        StudentDashboardMetric metric = buildMetric(2, weekly);
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));

        InteractionAnalyticsResponse response = service.execute(userId);

        assertThat(response.getPeakActivityDay()).isEqualTo(DayOfWeek.WEDNESDAY);
    }

    @Test
    void peakDayEsNullCuandoWeeklyActivityEsNull() {
        StudentDashboardMetric metric = StudentDashboardMetric.builder()
                .userId(userId)
                .patchesAttended(3)
                .weeklyActivity(null)
                .computedAt(LocalDateTime.now())
                .build();
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.of(metric));

        InteractionAnalyticsResponse response = service.execute(userId);

        assertThat(response.getPeakActivityDay()).isNull();
    }

    @Test
    void summaryMapContieneTodasLasClaves() {
        when(studentMetricsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        InteractionAnalyticsResponse response = service.execute(userId);

        assertThat(response.getInteractionSummary()).containsKeys("parcheAttendance", "rsvpConfirmed", "connectionRequest");
    }

    private StudentDashboardMetric buildMetric(int patches, Map<DayOfWeek, Integer> weekly) {
        return StudentDashboardMetric.builder()
                .userId(userId)
                .period(LocalDate.now())
                .patchesAttended(patches)
                .weeklyActivity(weekly)
                .computedAt(LocalDateTime.now())
                .build();
    }
}
