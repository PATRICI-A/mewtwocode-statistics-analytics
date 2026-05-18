package edu.eci.patriciaM12;

import edu.eci.patriciaM12.domain.exceptions.CsvGenerationException;
import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.StudentDashboardMetric;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import edu.eci.patriciaM12.infrastructure.adapters.adapter.AdminSnapshotRepositoryAdapter;
import edu.eci.patriciaM12.infrastructure.adapters.adapter.CsvGeneratorAdapter;
import edu.eci.patriciaM12.infrastructure.adapters.adapter.ReportRequestRepositoryAdapter;
import edu.eci.patriciaM12.infrastructure.adapters.adapter.StudentMetricsRepositoryAdapter;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.AdminAnalyticsSnapshotEntity;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.ReportRequestEntity;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.StudentDashboardMetricEntity;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper.AdminAnalyticsSnapshotMapper;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper.ReportRequestMapper;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper.StudentMetricsMapper;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.repository.AdminSnapshotJpaRepository;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.repository.ReportRequestJpaRepository;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.repository.StudentMetricsJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void csvGeneratorCreaArchivoConFiltros() throws Exception {
        CsvGeneratorAdapter adapter = new CsvGeneratorAdapter();
        ReflectionTestUtils.setField(adapter, "outputDir", tempDir.toString());
        ReportFilters filters = ReportFilters.builder()
                .dateFrom(LocalDate.of(2026, 1, 1))
                .dateTo(LocalDate.of(2026, 1, 31))
                .category(PatchCategory.GAMING)
                .campusZone(CampusZone.CAFETERIA)
                .build();

        String fileUrl = adapter.generate(filters);

        assertThat(fileUrl).startsWith(tempDir.toString());
        assertThat(Files.exists(Path.of(fileUrl))).isTrue();
        assertThat(Files.readString(Path.of(fileUrl))).contains("dateFrom", "GAMING", "CAFETERIA");
    }

    @Test
    void csvGeneratorEnvuelveErroresDeEscritura() throws Exception {
        CsvGeneratorAdapter adapter = new CsvGeneratorAdapter();
        Path outputFile = tempDir.resolve("report.csv");
        Files.writeString(outputFile, "ya existe");
        ReflectionTestUtils.setField(adapter, "outputDir", outputFile.toString());
        ReportFilters filters = ReportFilters.builder()
                .dateFrom(LocalDate.of(2026, 1, 1))
                .dateTo(LocalDate.of(2026, 1, 31))
                .build();

        assertThatThrownBy(() -> adapter.generate(filters))
                .isInstanceOf(CsvGenerationException.class)
                .hasMessageContaining("Error generating CSV report");
    }

    @Test
    void reportRequestRepositoryAdapterGuardaYBuscaPorId() {
        ReportRequestJpaRepository repository = mock(ReportRequestJpaRepository.class);
        ReportRequestMapper mapper = new ReportRequestMapper(new ObjectMapper());
        ReportRequestRepositoryAdapter adapter = new ReportRequestRepositoryAdapter(repository, mapper);
        UUID reportId = UUID.randomUUID();
        ReportRequest domain = ReportRequest.builder()
                .id(reportId)
                .status(ReportStatus.PENDING)
                .build();
        ReportRequestEntity savedEntity = ReportRequestEntity.builder()
                .id(reportId)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        when(repository.save(any())).thenReturn(savedEntity);
        when(repository.findById(reportId)).thenReturn(Optional.of(savedEntity));

        ReportRequest saved = adapter.save(domain);
        assertThat(saved.getId()).isEqualTo(reportId);
        assertThat(saved.getStatus()).isEqualTo(ReportStatus.PENDING);
        Optional<ReportRequest> found = adapter.findById(reportId);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(reportId);
    }

    @Test
    void studentMetricsRepositoryAdapterGuardaYBuscaPorUsuario() {
        StudentMetricsJpaRepository repository = mock(StudentMetricsJpaRepository.class);
        StudentMetricsMapper mapper = new StudentMetricsMapper(new ObjectMapper());
        StudentMetricsRepositoryAdapter adapter = new StudentMetricsRepositoryAdapter(repository, mapper);
        UUID userId = UUID.randomUUID();
        StudentDashboardMetric domain = StudentDashboardMetric.builder()
                .userId(userId)
                .patchesAttended(3)
                .weeklyActivity(new EnumMap<>(DayOfWeek.class))
                .computedAt(LocalDateTime.now())
                .build();
        StudentDashboardMetricEntity savedEntity = StudentDashboardMetricEntity.builder()
                .userId(userId)
                .patchesAttended(3)
                .build();
        when(repository.save(any())).thenReturn(savedEntity);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(savedEntity));

        StudentDashboardMetric saved = adapter.save(domain);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getPatchesAttended()).isEqualTo(3);
        Optional<StudentDashboardMetric> found = adapter.findByUserId(userId);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void adminSnapshotRepositoryAdapterGuardaYConsultaRangos() {
        AdminSnapshotJpaRepository repository = mock(AdminSnapshotJpaRepository.class);
        AdminAnalyticsSnapshotMapper mapper = new AdminAnalyticsSnapshotMapper();
        AdminSnapshotRepositoryAdapter adapter = new AdminSnapshotRepositoryAdapter(repository, mapper);
        UUID snapshotId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 2, 1);
        AdminAnalyticsSnapshot domain = AdminAnalyticsSnapshot.builder()
                .id(snapshotId)
                .snapshotDate(date)
                .activeUsers(10)
                .totalPatches(5)
                .generatedAt(LocalDateTime.now())
                .build();
        AdminAnalyticsSnapshotEntity savedEntity = AdminAnalyticsSnapshotEntity.builder()
                .id(snapshotId)
                .snapshotDate(date)
                .activeUsers(10)
                .totalPatches(5)
                .topCategories("[]")
                .generatedAt(LocalDateTime.now())
                .build();
        when(repository.save(any())).thenReturn(savedEntity);
        when(repository.findBySnapshotDate(date)).thenReturn(Optional.of(savedEntity));
        when(repository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(date, date.plusDays(1)))
                .thenReturn(List.of(savedEntity));

        AdminAnalyticsSnapshot saved = adapter.save(domain);
        assertThat(saved.getId()).isEqualTo(snapshotId);
        assertThat(saved.getActiveUsers()).isEqualTo(10);
        Optional<AdminAnalyticsSnapshot> found = adapter.findByDate(date);
        assertThat(found).isPresent();
        assertThat(found.get().getSnapshotDate()).isEqualTo(date);
        List<AdminAnalyticsSnapshot> range = adapter.findByDateRange(date, date.plusDays(1));
        assertThat(range).hasSize(1);
        assertThat(range.get(0).getTotalPatches()).isEqualTo(5);
    }
}