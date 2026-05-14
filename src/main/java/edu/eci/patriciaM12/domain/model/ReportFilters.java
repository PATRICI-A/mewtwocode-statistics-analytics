package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * Immutable value object that encapsulates the filter criteria used when requesting
 * an analytics report.  All fields are optional; null values are treated as
 * "no filter applied" for the corresponding dimension.
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

    /** Inclusive start date of the reporting period; {@code null} means no lower bound. */
    LocalDate dateFrom;

    /** Inclusive end date of the reporting period; {@code null} means no upper bound. */
    LocalDate dateTo;

    /** When {@code true}, admin-level aggregated data is included in the generated report. */
    boolean includeAdmin;
}