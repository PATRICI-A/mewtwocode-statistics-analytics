package edu.eci.patriciaM12.application.dto.request;

import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Incoming request payload carrying the filter criteria used to generate a CSV report.
 * Both {@code dateFrom} and {@code dateTo} are mandatory; the remaining fields are optional
 * and default to no restriction when omitted.
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
     * Converts this request object into the domain {@link ReportFilters} model.
     *
     * @return a fully populated {@link ReportFilters} instance built from the fields of this request
     */
    public ReportFilters toDomain() {
        return ReportFilters.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .category(category)
                .campusZone(campusZone)
                .includeAdmin(includeAdmin)
                .build();
    }
}