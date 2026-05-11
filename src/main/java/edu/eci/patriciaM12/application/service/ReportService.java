package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException;
import edu.eci.patriciaM12.domain.exceptions.ReportNotFoundException;
import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import edu.eci.patriciaM12.domain.ports.in.RequestReportUseCase;
import edu.eci.patriciaM12.domain.ports.out.CsvGeneratorPort;
import edu.eci.patriciaM12.domain.ports.out.ReportRequestRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService implements RequestReportUseCase {

    private final ReportRequestRepositoryPort reportRequestRepository;
    private final CsvGeneratorPort csvGenerator;

    @Override
    public ReportRequest create(UUID requestedBy, ReportFilters filters) {
        if (filters.getDateFrom().isAfter(filters.getDateTo())) {
            throw new InvalidReportFiltersException("Fechas invalidas");
        }

        ReportRequest pending = ReportRequest.builder()
                .id(UUID.randomUUID())
                .requestedBy(requestedBy)
                .dateFrom(filters.getDateFrom())
                .dateTo(filters.getDateTo())
                .filters(filters)
                .status(ReportStatus.PENDING)
                .fileUrl(null)
                .build();

        ReportRequest saved = reportRequestRepository.save(pending);
        generateAsync(saved);
        return saved;
    }

    @Override
    public ReportRequest findById(UUID reportId, UUID requestedBy) {
        return reportRequestRepository.findById(reportId)
                .filter(r -> r.getRequestedBy().equals(requestedBy))
                .orElseThrow(() -> new ReportNotFoundException(reportId));
    }

    @Async
    protected void generateAsync(ReportRequest report) {
        try {
            String fileUrl = csvGenerator.generate(report.getFilters());
            reportRequestRepository.save(buildWith(report, ReportStatus.READY, fileUrl));
        } catch (Exception ex) {
            reportRequestRepository.save(buildWith(report, ReportStatus.FAILED, null));
        }
    }

    private ReportRequest buildWith(ReportRequest report, ReportStatus status, String fileUrl) {
        return ReportRequest.builder()
                .id(report.getId())
                .requestedBy(report.getRequestedBy())
                .dateFrom(report.getDateFrom())
                .dateTo(report.getDateTo())
                .filters(report.getFilters())
                .status(status)
                .fileUrl(fileUrl)
                .build();
    }
}