package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.application.dto.request.ScheduleDTO;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.model.enums.ExportFormat;
import edu.eci.patriciaM12.domain.model.enums.MetricType;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

/**
 * Immutable value object that encapsulates the filter criteria used when requesting
 * an analytics report (RF-19).  All fields except {@code dateFrom}/{@code dateTo} are optional;
 * {@code null} values are treated as "no filter applied" for the corresponding dimension.
 *
 * <p>An {@link edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException}
 * is thrown by the use-case layer if the combination of filters is logically
 * inconsistent (e.g., {@code dateFrom} after {@code dateTo}).</p>
 */
@Value
@Builder
public class ReportFilters {

    /** Restricts the report to patches belonging to this category; {@code null} means all categories. */
    PatchCategory category;

    /** Restricts the report to patches held in this campus zone; {@code null} means all zones. */
    CampusZone campusZone;

    /** Inclusive start date of the reporting period. */
    LocalDate dateFrom;

    /** Inclusive end date of the reporting period. */
    LocalDate dateTo;

    /** When {@code true}, admin-level aggregated data is included in the generated report. */
    boolean includeAdmin;

    /**
     * When {@code true}, the service returns a preview of the first 10 rows without
     * persisting a file (RF-19 RN-19.4).
     */
    boolean preview;

    /**
     * Subset of metric dimensions to include; {@code null} means all metrics are included.
     */
    List<MetricType> metrics;

    /**
     * Target export format; defaults to {@link ExportFormat#CSV} when {@code null}.
     */
    ExportFormat format;

    /**
     * Optional recurring delivery configuration.  When present the report service registers
     * a scheduled job (RF-19 RN-19.6).
     */
    ScheduleDTO schedule;
}
