package edu.eci.patriciaM12;

import edu.eci.patriciaM12.application.dto.request.ReportFiltersRequest;
import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
import edu.eci.patriciaM12.application.dto.response.AnalyticsDTO;
import edu.eci.patriciaM12.application.dto.response.HeatmapDTO;
import edu.eci.patriciaM12.application.dto.response.ReportRequestResponse;
import edu.eci.patriciaM12.application.dto.response.StudentDashboardResponse;
import edu.eci.patriciaM12.domain.model.CategoryStat;
import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.model.enums.ParticipationLevel;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DtoMappingTest {

    @Test
    void reportFiltersRequestToDomainCopiaTodosLosCampos() {
        ReportFiltersRequest request = new ReportFiltersRequest();
        request.setDateFrom(LocalDate.of(2026, 1, 1));
        request.setDateTo(LocalDate.of(2026, 1, 31));
        request.setCategory(PatchCategory.CULTURE);
        request.setCampusZone(CampusZone.BIBLIOTECA);
        request.setIncludeAdmin(true);

        ReportFilters filters = request.toDomain();

        assertThat(filters.getDateFrom()).isEqualTo(request.getDateFrom());
        assertThat(filters.getDateTo()).isEqualTo(request.getDateTo());
        assertThat(filters.getCategory()).isEqualTo(PatchCategory.CULTURE);
        assertThat(filters.getCampusZone()).isEqualTo(CampusZone.BIBLIOTECA);
        assertThat(filters.isIncludeAdmin()).isTrue();
    }

    @Test
    void studentDashboardResponseFromCopiaMetricaCalculada() {
        UUID userId = UUID.randomUUID();
        LocalDateTime computedAt = LocalDateTime.now();
        Map<DayOfWeek, Integer> weekly = new EnumMap<>(DayOfWeek.class);
        weekly.put(DayOfWeek.MONDAY, 2);

        StudentDashboardMetric metric = StudentDashboardMetric.builder()
                .userId(userId)
                .patchesAttended(12)
                .topCategory(PatchCategory.STUDY)
                .weeklyActivity(weekly)
                .computedAt(computedAt)
                .build();

        StudentDashboardResponse response = StudentDashboardResponse.from(metric);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getPatchesAttended()).isEqualTo(12);
        assertThat(response.getTopCategory()).isEqualTo(PatchCategory.STUDY);
        assertThat(response.getWeeklyActivity()).containsEntry(DayOfWeek.MONDAY, 2);
        assertThat(response.getParticipationLevel()).isEqualTo(ParticipationLevel.CONECTOR);
        assertThat(response.getComputedAt()).isEqualTo(computedAt);
    }

    @Test
    void reportRequestResponseFromCopiaEstadoYArchivo() {
        UUID reportId = UUID.randomUUID();
        ReportRequest report = ReportRequest.builder()
                .id(reportId)
                .status(ReportStatus.READY)
                .fileUrl("/tmp/report.csv")
                .build();

        ReportRequestResponse response = ReportRequestResponse.from(report);

        assertThat(response.getId()).isEqualTo(reportId);
        assertThat(response.getStatus()).isEqualTo(ReportStatus.READY);
        assertThat(response.getFileUrl()).isEqualTo("/tmp/report.csv");
    }

    @Test
    void buildersDeAnalyticsExponenSusValores() {
        LocalDate date = LocalDate.of(2026, 2, 1);
        CategoryStat categoryStat = CategoryStat.builder()
                .category(PatchCategory.SPORTS)
                .count(5)
                .percentage(25.5f)
                .build();
        AnalyticsDTO activeUsers = AnalyticsDTO.builder()
                .timeSeries(Map.of(date, 10))
                .total(10)
                .categories(List.of(categoryStat))
                .build();
        HeatmapDTO heatmap = HeatmapDTO.builder().build();

        AdminAnalyticsResponse response = AdminAnalyticsResponse.builder()
                .activeUsers(activeUsers)
                .parcheStats(activeUsers)
                .topEvents(List.of())
                .matchSuccessRate(0.75)
                .campusHeatmap(heatmap)
                .build();

        assertThat(activeUsers.getTimeSeries()).containsEntry(date, 10);
        assertThat(activeUsers.getTotal()).isEqualTo(10);
        assertThat(activeUsers.getCategories()).containsExactly(categoryStat);
        assertThat(categoryStat.getCategory()).isEqualTo(PatchCategory.SPORTS);
        assertThat(categoryStat.getCount()).isEqualTo(5);
        assertThat(categoryStat.getPercentage()).isEqualTo(25.5f);
        assertThat(response.getActiveUsers()).isEqualTo(activeUsers);
        assertThat(response.getParcheStats()).isEqualTo(activeUsers);
        assertThat(response.getTopEvents()).isEmpty();
        assertThat(response.getMatchSuccessRate()).isEqualTo(0.75);
        assertThat(response.getCampusHeatmap()).isSameAs(heatmap);
    }
}