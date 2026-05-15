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
                .hasMessageContaining("Error generando reporte CSV");
    }

    @Test
    void reportRequestRepositoryAdapterGuardaYBuscaPorId() {
        ReportRequestJpaRepository repository = mock(ReportRequestJpaRepository.class);
        ReportRequestMapper mapper = mock(ReportRequestMapper.class);
        ReportRequestRepositoryAdapter adapter = new ReportRequestRepositoryAdapter(repository, mapper);
        UUID reportId = UUID.randomUUID();
        ReportRequest domain = ReportRequest.builder()
                .id(reportId)
                .status(ReportStatus.PENDING)
                .build();
        ReportRequestEntity entity = ReportRequestEntity.builder()
                .id(reportId)
                .status(ReportStatus.PENDING)
                .build();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(repository.findById(reportId)).thenReturn(Optional.of(entity));

        assertThat(adapter.save(domain)).isSameAs(domain);
        assertThat(adapter.findById(reportId)).containsSame(domain);
    }

    @Test
    void studentMetricsRepositoryAdapterGuardaYBuscaPorUsuario() {
        StudentMetricsJpaRepository repository = mock(StudentMetricsJpaRepository.class);
        StudentMetricsMapper mapper = mock(StudentMetricsMapper.class);
        StudentMetricsRepositoryAdapter adapter = new StudentMetricsRepositoryAdapter(repository, mapper);
        UUID userId = UUID.randomUUID();
        StudentDashboardMetric domain = StudentDashboardMetric.builder()
                .userId(userId)
                .weeklyActivity(new EnumMap<>(DayOfWeek.class))
                .build();
        StudentDashboardMetricEntity entity = StudentDashboardMetricEntity.builder()
                .userId(userId)
                .build();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(entity));

        assertThat(adapter.save(domain)).isSameAs(domain);
        assertThat(adapter.findByUserId(userId)).containsSame(domain);
    }

    @Test
    void adminSnapshotRepositoryAdapterGuardaYConsultaRangos() {
        AdminSnapshotJpaRepository repository = mock(AdminSnapshotJpaRepository.class);
        AdminAnalyticsSnapshotMapper mapper = mock(AdminAnalyticsSnapshotMapper.class);
        AdminSnapshotRepositoryAdapter adapter = new AdminSnapshotRepositoryAdapter(repository, mapper);
        UUID snapshotId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 2, 1);
        AdminAnalyticsSnapshot domain = AdminAnalyticsSnapshot.builder()
                .id(snapshotId)
                .snapshotDate(date)
                .generatedAt(LocalDateTime.now())
                .build();
        AdminAnalyticsSnapshotEntity entity = AdminAnalyticsSnapshotEntity.builder()
                .id(snapshotId)
                .snapshotDate(date)
                .build();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(repository.findBySnapshotDate(date)).thenReturn(Optional.of(entity));
        when(repository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(date, date.plusDays(1)))
                .thenReturn(List.of(entity));

        assertThat(adapter.save(domain)).isSameAs(domain);
        assertThat(adapter.findByDate(date)).containsSame(domain);
        assertThat(adapter.findByDateRange(date, date.plusDays(1))).containsExactly(domain);
    }
}