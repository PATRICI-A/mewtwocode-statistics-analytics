package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class ReportFilters {
    PatchCategory category;
    CampusZone campusZone;
    LocalDate dateFrom;
    LocalDate dateTo;
    boolean includeAdmin;
}