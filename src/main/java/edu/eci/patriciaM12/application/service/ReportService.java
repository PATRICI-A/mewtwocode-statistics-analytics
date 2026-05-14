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

/**
 * Application service that implements the {@link RequestReportUseCase} use case.
 * <p>
 * Manages the full lifecycle of a CSV report request:
 * <ol>
 *   <li>Validates the supplied date filters.</li>
 *   <li>Persists the request with status {@code PENDING}.</li>
 *   <li>Triggers asynchronous CSV generation via {@link #generateAsync(ReportRequest)}.</li>
 *   <li>Updates the persisted record to {@code READY} on success or {@code FAILED} on error.</li>
 * </ol>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ReportService implements RequestReportUseCase {

    private final ReportRequestRepositoryPort reportRequestRepository;
    private final CsvGeneratorPort csvGenerator;

    /**
     * Creates and persists a new report request, then triggers asynchronous CSV generation.
     * The returned object always has status {@code PENDING}.
     *
     * @param requestedBy the UUID of the user requesting the report
     * @param filters     the filter criteria that will govern the CSV content
     * @return the newly persisted {@link ReportRequest} with status {@code PENDING}
     * @throws edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException if
     *         {@code dateFrom} is after {@code dateTo}
     */
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

    /**
     * Retrieves a report request by its ID, enforcing that it belongs to the requesting user.
     *
     * @param reportId    the UUID of the report request to retrieve
     * @param requestedBy the UUID of the user who originally requested the report
     * @return the matching {@link ReportRequest}
     * @throws edu.eci.patriciaM12.domain.exceptions.ReportNotFoundException if no report with
     *         the given ID exists or it does not belong to {@code requestedBy}
     */
    @Override
    public ReportRequest findById(UUID reportId, UUID requestedBy) {
        return reportRequestRepository.findById(reportId)
                .filter(r -> r.getRequestedBy().equals(requestedBy))
                .orElseThrow(() -> new ReportNotFoundException(reportId));
    }

    /**
     * Asynchronously generates the CSV file for the given report request and updates its status.
     * <p>
     * Runs in a Spring-managed thread pool (annotated with {@code @Async}).
     * On success the report status is set to {@code READY} and the file URL is persisted.
     * On any exception the status is set to {@code FAILED} and the file URL remains {@code null}.
     * </p>
     *
     * @param report the report request for which the CSV should be generated
     */
    @Async
    protected void generateAsync(ReportRequest report) {
        try {
            String fileUrl = csvGenerator.generate(report.getFilters());
            reportRequestRepository.save(buildWith(report, ReportStatus.READY, fileUrl));
        } catch (Exception ex) {
            reportRequestRepository.save(buildWith(report, ReportStatus.FAILED, null));
        }
    }

    /**
     * Creates a new {@link ReportRequest} that is identical to the given one except for the
     * status and file URL, which are replaced with the supplied values.
     *
     * @param report  the original report request whose fields will be copied
     * @param status  the new status to set on the returned request
     * @param fileUrl the file URL to set, or {@code null} when the report failed
     * @return a new {@link ReportRequest} with the updated status and file URL
     */
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