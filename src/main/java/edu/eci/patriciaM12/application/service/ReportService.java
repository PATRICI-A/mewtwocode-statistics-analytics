package edu.eci.patriciaM12.application.service;

import edu.eci.patriciaM12.application.dto.event.ReportReadyEventDto;
import edu.eci.patriciaM12.application.dto.response.ReportHistoryDTO;
import edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException;
import edu.eci.patriciaM12.domain.exceptions.ReportNotFoundException;
import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import edu.eci.patriciaM12.domain.ports.in.RequestReportUseCase;
import edu.eci.patriciaM12.domain.ports.out.CsvGeneratorPort;
import edu.eci.patriciaM12.domain.ports.out.NotificationPublisherPort;
import edu.eci.patriciaM12.domain.ports.out.ReportRequestRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service that implements the {@link RequestReportUseCase} use case (RF-19).
 * <p>
 * Manages the full lifecycle of a CSV report request:
 * <ol>
 *   <li>Validates the supplied date filters.</li>
 *   <li>When {@code preview} is {@code true}, returns the first 10 rows without persisting a
 *       file (RN-19.4).</li>
 *   <li>For standard requests, persists the request as {@code PENDING} and triggers
 *       asynchronous generation via {@link #generateAsync(ReportRequest)}.</li>
 *   <li>Reports with more than 10,000 records are always processed asynchronously (RN-19.3).</li>
 *   <li>Updates the persisted record to {@code READY} on success or {@code FAILED} on error.</li>
 * </ol>
 * Generated files are retained for 30 days; the {@code expiresAt} timestamp in report history
 * entries reflects this retention policy (RN-19.5).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ReportService implements RequestReportUseCase {

    private static final int ASYNC_THRESHOLD = 10_000;
    private static final int PREVIEW_LIMIT = 10;
    private static final int REPORT_RETENTION_DAYS = 30;

    private final ReportRequestRepositoryPort reportRequestRepository;
    private final CsvGeneratorPort csvGenerator;
    private final NotificationPublisherPort notificationPublisher;

    /**
     * Creates and persists a new report request, then triggers CSV generation.
     * <p>
     * When {@code filters.isPreview()} is {@code true}, the method invokes
     * {@link CsvGeneratorPort#generatePreview} and returns immediately without persisting.
     * Otherwise a {@code PENDING} record is saved and asynchronous generation is queued.
     * </p>
     *
     * @param requestedBy the UUID of the user requesting the report
     * @param filters     the filter criteria governing the CSV content
     * @return the newly persisted {@link ReportRequest} with status {@code PENDING},
     *         or a synthetic preview {@link ReportRequest} when preview mode is active
     * @throws InvalidReportFiltersException if {@code dateFrom} is after {@code dateTo}
     */
    @Override
    public ReportRequest create(UUID requestedBy, ReportFilters filters) {
        if (filters.getDateFrom().isAfter(filters.getDateTo())) {
            throw new InvalidReportFiltersException("dateFrom must not be after dateTo.");
        }

        if (filters.isPreview()) {
            return createPreview(requestedBy, filters);
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
     * @throws ReportNotFoundException if no report with the given ID exists for the user
     */
    @Override
    public ReportRequest findById(UUID reportId, UUID requestedBy) {
        return reportRequestRepository.findById(reportId)
                .filter(r -> r.getRequestedBy().equals(requestedBy))
                .orElseThrow(() -> new ReportNotFoundException(reportId));
    }

    /**
     * Returns the report history for the given user.
     * Each entry includes the report ID, generation timestamp, download URL, and expiry date.
     * Reports older than {@value #REPORT_RETENTION_DAYS} days are excluded (RN-19.5).
     *
     * @param requestedBy the UUID of the user whose history is requested
     * @return list of {@link ReportHistoryDTO} items; never {@code null}
     */
    @Override
    public List<ReportHistoryDTO> getHistory(UUID requestedBy) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(REPORT_RETENTION_DAYS);
        return reportRequestRepository.findAllByRequestedBy(requestedBy).stream()
                .filter(r -> r.getStatus() == ReportStatus.READY)
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(cutoff))
                .map(r -> ReportHistoryDTO.builder()
                        .reportId(r.getId())
                        .generatedAt(r.getCreatedAt())
                        .metrics(r.getFilters() != null ? r.getFilters().getMetrics() : null)
                        .downloadUrl(r.getFileUrl())
                        .expiresAt(r.getCreatedAt().plusDays(REPORT_RETENTION_DAYS))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Executes a preview generation: delegates to {@link CsvGeneratorPort#generatePreview} and
     * returns a synthetic {@link ReportRequest} in state {@code READY} with the preview file URL.
     * No record is persisted.
     *
     * @param requestedBy the UUID of the user requesting the preview
     * @param filters     the filter criteria
     * @return a synthetic {@link ReportRequest} with the preview file URL and status {@code READY}
     */
    private ReportRequest createPreview(UUID requestedBy, ReportFilters filters) {
        String previewUrl = csvGenerator.generatePreview(filters, PREVIEW_LIMIT);
        return ReportRequest.builder()
                .id(UUID.randomUUID())
                .requestedBy(requestedBy)
                .dateFrom(filters.getDateFrom())
                .dateTo(filters.getDateTo())
                .filters(filters)
                .status(ReportStatus.READY)
                .fileUrl(previewUrl)
                .build();
    }

    /**
     * Asynchronously generates the CSV file for the given report request and updates its status.
     * Runs in a Spring-managed thread pool ({@code @Async}).
     *
     * @param report the report request for which the CSV should be generated
     */
    @Async
    protected void generateAsync(ReportRequest report) {
        try {
            String fileUrl = csvGenerator.generate(report.getFilters());
            reportRequestRepository.save(buildWith(report, ReportStatus.READY, fileUrl));
            // Notify user that their report is ready for download (REPORT_READY)
            notificationPublisher.publishReportReady(
                    ReportReadyEventDto.builder()
                            .requestedBy(report.getRequestedBy())
                            .reportId(report.getId())
                            .downloadUrl(fileUrl)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        } catch (Exception ex) {
            reportRequestRepository.save(buildWith(report, ReportStatus.FAILED, null));
        }
    }

    /**
     * Creates a copy of the given report request with a new status and file URL.
     *
     * @param report  the original report request
     * @param status  the new status
     * @param fileUrl the new file URL; {@code null} when failed
     * @return the updated {@link ReportRequest}
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
