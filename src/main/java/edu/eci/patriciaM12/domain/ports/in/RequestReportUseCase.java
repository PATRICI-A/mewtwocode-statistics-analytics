package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.ReportFilters;

import java.util.UUID;

public interface RequestReportUseCase {
    ReportRequest create(UUID requestedBy, ReportFilters filters);
    ReportRequest findById(UUID reportId, UUID requestedBy);
}
