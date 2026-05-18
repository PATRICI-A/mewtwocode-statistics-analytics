package edu.eci.patriciaM12.application.dto.request;

import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.model.enums.ExportFormat;
import edu.eci.patriciaM12.domain.model.enums.MetricType;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
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
 *
 * <ul>
 *   <li>{@code preview} — when {@code true}, returns the first 10 rows without persisting a file (RN-19.4)</li>
 *   <li>{@code schedule} — when set, registers a recurring delivery job (RN-19.6)</li>
 *   <li>{@code metrics} — list of metric dimensions to include; {@code null} means all (RN-19.2)</li>
 *   <li>{@code format} — export file format; defaults to {@link ExportFormat#CSV} when absent</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class ReportFiltersRequest {

    @NotNull
    private LocalDate dateFrom;

    @NotNull
    private LocalDate dateTo;

    private PatchCategory category;
    private CampusZone campusZone;
    private boolean includeAdmin;

    /**
     * When {@code true}, the endpoint returns a preview of the first 10 data rows instead of
     * generating and persisting a full report file (RF-19 RN-19.4).
     */
    private boolean preview;

    /**
     * Optional nested date-range object.  When present, its {@code startDate}/{@code endDate}
     * override the top-level {@code dateFrom}/{@code dateTo} fields.
     */
    @Valid
    private DateRangeDTO dateRange;

    /**
     * Subset of metric dimensions to include in the report.
     * {@code null} or empty means all available metrics are included.
     */
    private List<MetricType> metrics;

    /**
     * Target export format.  Defaults to {@link ExportFormat#CSV} when {@code null}.
     */
    private ExportFormat format;

    /**
     * Optional recurring delivery schedule.  When set, the report service registers a job
     * that regenerates and sends the report at the configured frequency (RF-19 RN-19.6).
     */
    @Valid
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
