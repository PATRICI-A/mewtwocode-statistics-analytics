package edu.eci.patriciaM12.application.dto.request;

import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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