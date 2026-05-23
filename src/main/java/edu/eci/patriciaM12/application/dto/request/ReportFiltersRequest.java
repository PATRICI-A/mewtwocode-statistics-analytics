package edu.eci.patriciaM12.application.dto.request;

import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.model.enums.ExportFormat;
import edu.eci.patriciaM12.domain.model.enums.MetricType;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Incoming request payload carrying the filter criteria used to generate an analytics report (RF-19).
 * <p>
 * Both {@code dateFrom} and {@code dateTo} are mandatory when {@code dateRange} is absent.
 * If {@code dateRange} is populated it takes precedence over the flat date fields.
 * </p>
 */
@Data
@NoArgsConstructor
@Schema(
        name = "ReportFiltersRequest",
        description = """
                Filter criteria for CSV report generation (RF-19). Both `dateFrom` and `dateTo` \
                are required unless a `dateRange` object is provided (which takes precedence). \
                Supports preview mode (`preview: true`) for synchronous 10-row previews without \
                file persistence (RN-19.4), and scheduled recurring delivery with configurable \
                frequency (RN-19.6)."""
)
public class ReportFiltersRequest {

    @NotNull
    @Schema(
            description = "Inclusive start date of the reporting period in ISO-8601 format (yyyy-MM-dd). Required when `dateRange` is not provided.",
            example = "2025-02-01"
    )
    private LocalDate dateFrom;

    @NotNull
    @Schema(
            description = "Inclusive end date of the reporting period in ISO-8601 format (yyyy-MM-dd). Required when `dateRange` is not provided.",
            example = "2025-06-30"
    )
    private LocalDate dateTo;

    @Schema(
            description = "Filter by parche category. When provided, only parches matching this category are included.",
            example = "SPORTS"
    )
    private PatchCategory category;

    @Schema(
            description = "Filter by campus zone. When provided, only interactions occurring in this zone are included.",
            example = "CAFETERIA"
    )
    private CampusZone campusZone;

    @Schema(
            description = """
                    When `true`, includes administrative parches (created by staff) in the report data. \
                    When `false` (default), only user-created public parches are included.""",
            example = "false"
    )
    private boolean includeAdmin;

    @Schema(
            description = """
                    **Preview mode flag (RN-19.4):** When `true`, the endpoint returns the first 10 data rows \
                    synchronously without persisting a report file. The response status is HTTP 200 with `READY` \
                    status and the preview file URL already populated. No file is persisted; results are ephemeral.""",
            example = "false"
    )
    private boolean preview;

    @Valid
    @Schema(
            description = """
                    Optional nested date-range object. When present, its `startDate` and `endDate` override \
                    the top-level `dateFrom`/`dateTo` fields. Useful for callers that prefer a structured \
                    date representation."""
    )
    private DateRangeDTO dateRange;

    @Schema(
            description = """
                    Subset of metric dimensions to include in the report. Acceptable values: \
                    `USERS`, `PARCHES`, `EVENTS`, `MATCHES`, `ZONES`. \
                    When `null` or empty, all available metrics are included (RN-19.2).""",
            example = "[\"USERS\", \"PARCHES\", \"EVENTS\"]"
    )
    private List<MetricType> metrics;

    @Schema(
            description = """
                    Target export format for the report file. Supported values: `CSV`, `JSON`. \
                    Defaults to `CSV` when absent.""",
            defaultValue = "CSV",
            example = "CSV"
    )
    private ExportFormat format;

    @Valid
    @Schema(
            description = """
                    Optional recurring delivery schedule configuration. When provided, registers a background \
                    job that automatically regenerates and delivers the report at the configured frequency (RF-19 RN-19.6)."""
    )
    private ScheduleDTO schedule;

    /**
     * Converts this request object into the domain {@link ReportFilters} model.
     * If {@code dateRange} is present its dates take precedence.
     *
     * @return a fully populated {@link ReportFilters} instance built from the fields of this request
     */
    public ReportFilters toDomain() {
        LocalDate from = dateRange != null ? dateRange.getStartDate() : dateFrom;
        LocalDate to = dateRange != null ? dateRange.getEndDate() : dateTo;
        return ReportFilters.builder()
                .dateFrom(from)
                .dateTo(to)
                .category(category)
                .campusZone(campusZone)
                .includeAdmin(includeAdmin)
                .preview(preview)
                .metrics(metrics)
                .format(format != null ? format : ExportFormat.CSV)
                .schedule(schedule)
                .build();
    }
}