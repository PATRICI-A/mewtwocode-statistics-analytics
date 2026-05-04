package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReportRequest {

    private UUID id;
    private UUID requestedBy;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private ReportFilters filters;
    private ReportStatus status;
    private String fileUrl;

}