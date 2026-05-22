package edu.eci.patriciaM12;

import edu.eci.patriciaM12.application.dto.response.SocialIndicatorsResponse;
import edu.eci.patriciaM12.application.service.SocialIndicatorsService;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.ActivityLevel;
import edu.eci.patriciaM12.domain.ports.out.StudentMetricsRepositoryPort;
import edu.eci.patriciaM12.infrastructure.external.CampusEventsFeignClient;
import edu.eci.patriciaM12.infrastructure.external.HangoutFeignClient;
import edu.eci.patriciaM12.infrastructure.external.ProfileFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialIndicatorsServiceTest {

    @Mock private StudentMetricsRepositoryPort studentMetricsRepository;
    @Mock private HangoutFeignClient hangoutFeignClient;
    @Mock private CampusEventsFeignClient campusEventsFeignClient;
    @Mock private ProfileFeignClient profileFeignClient;

    private SocialIndicatorsService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SocialIndicatorsService(studentMetricsRepository, hangoutFeignClient, campusEventsFeignClient, profileFeignClient);
    }

    @Test
    void retornaIndicadoresVaciosCuandoNoHayMetricas() {
        when(studentMetricsRepository.findByUserIdAndWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(studentMetricsRepository.findByUserIdAndPreviousWeek(any(), any(), any())).thenReturn(Optional.empty());

        SocialIndicatorsResponse response = service.execute(userId, null);

        assertThat(response).isNotNull();
        assertThat(response.getWeeklyParticipation().getParcheCount()).isZero();
        assertThat(response.getWeeklyParticipation().getEventRsvpCount()).isZero();
        assertThat(response.getActivityLevel()).isEqualTo(ActivityLevel.LOW);
    }

    @Test
    void weekRangeNullUsaSemanaActual() {
        when(studentMetricsRepository.findByUserIdAndWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(studentMetricsRepository.findByUserIdAndPreviousWeek(any(), any(), any())).thenReturn(Optional.empty());

        SocialIndicatorsResponse response = service.execute(userId, null);

        assertThat(response).isNotNull();
    }

    @Test
    void weekRangeCeroUsaSemanaActual() {
        when(studentMetricsRepository.findByUserIdAndWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(studentMetricsRepository.findByUserIdAndPreviousWeek(any(), any(), any())).thenReturn(Optional.empty());

        SocialIndicatorsResponse r0 = service.execute(userId, 0);
        SocialIndicatorsResponse rNull = service.execute(userId, null);

        assertThat(r0.getActivityLevel()).isEqualTo(rNull.getActivityLevel());
    }

    @Test
    void clasificaLowCuandoTotalEsCeroOUno() {
        when(studentMetricsRepository.findByUserIdAndWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(studentMetricsRepository.findByUserIdAndPreviousWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(hangoutFeignClient.getUserParcheCount(userId)).thenReturn(1);

        assertThat(service.execute(userId, 0).getActivityLevel()).isEqualTo(ActivityLevel.LOW);
    }

    @Test
    void clasificaMediumCuandoTotalEsDosACuatro() {
        when(studentMetricsRepository.findByUserIdAndWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(studentMetricsRepository.findByUserIdAndPreviousWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(hangoutFeignClient.getUserParcheCount(userId)).thenReturn(2);

        assertThat(service.execute(userId, 0).getActivityLevel()).isEqualTo(ActivityLevel.MEDIUM);
    }

    @Test
    void clasificaHighCuandoTotalEsCincoANueve() {
        when(studentMetricsRepository.findByUserIdAndWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(studentMetricsRepository.findByUserIdAndPreviousWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(hangoutFeignClient.getUserParcheCount(userId)).thenReturn(5);

        assertThat(service.execute(userId, 0).getActivityLevel()).isEqualTo(ActivityLevel.HIGH);
    }

    @Test
    void clasificaVeryHighCuandoTotalEsDiezOMas() {
        when(studentMetricsRepository.findByUserIdAndWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(studentMetricsRepository.findByUserIdAndPreviousWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(hangoutFeignClient.getUserParcheCount(userId)).thenReturn(10);

        assertThat(service.execute(userId, 0).getActivityLevel()).isEqualTo(ActivityLevel.VERY_HIGH);
    }

    @Test
    void afinidadSocialCaiculaCorrectamentePonderacion() {
        StudentDashboardMetric metric = buildMetric(10, emptyWeekly());
        when(studentMetricsRepository.findByUserIdAndWeek(any(), any(), any())).thenReturn(Optional.of(metric));
        when(studentMetricsRepository.findByUserIdAndPreviousWeek(any(), any(), any())).thenReturn(Optional.empty());

        SocialIndicatorsResponse response = service.execute(userId, 0);
        double totalScore = response.getSocialAffinity().getTotalScore();
        double expectedParche = 1.0 * 0.35;
        assertThat(totalScore).isEqualTo(expectedParche);
    }

    @Test
    void crecimientoDeRedEsNullCuandoPreviousEsCero() {
        when(studentMetricsRepository.findByUserIdAndWeek(any(), any(), any())).thenReturn(Optional.empty());
        when(studentMetricsRepository.findByUserIdAndPreviousWeek(any(), any(), any())).thenReturn(Optional.empty());

        SocialIndicatorsResponse response = service.execute(userId, 0);

        assertThat(response.getNetworkGrowth().getGrowthRate()).isNull();
        assertThat(response.getNetworkGrowth().getCurrentWeekConnections()).isZero();
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

    private Map<DayOfWeek, Integer> emptyWeekly() {
        Map<DayOfWeek, Integer> map = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) map.put(d, 0);
        return map;
    }
}
