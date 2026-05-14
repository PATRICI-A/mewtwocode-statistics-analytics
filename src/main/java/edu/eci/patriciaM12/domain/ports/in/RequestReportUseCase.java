package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.ReportFilters;

import java.util.UUID;

/**
 * Input port (primary port) that defines the use cases for managing asynchronous
 * analytics report requests.  Implementations coordinate validation, persistence,
 * and asynchronous CSV generation through the output ports.
 */
public interface RequestReportUseCase {

    /**
     * Creates a new report request with {@link edu.eci.patriciaM12.domain.model.enums.ReportStatus#PENDING}
     * status and enqueues it for asynchronous CSV generation.
     *
     * @param requestedBy the unique identifier of the user submitting the request
     * @param filters     the filter criteria to apply when generating the report
     * @return the persisted {@link ReportRequest} with its assigned identifier and initial status
     * @throws edu.eci.patriciaM12.domain.exceptions.InvalidReportFiltersException if the
     *         provided filters are logically invalid
     */
    ReportRequest create(UUID requestedBy, ReportFilters filters);

    /**
     * Retrieves an existing report request by its identifier, ensuring the caller is
     * the user who originally submitted it.
     *
     * @param reportId    the unique identifier of the report request to retrieve
     * @param requestedBy the unique identifier of the user requesting access
     * @return the matching {@link ReportRequest}
     * @throws edu.eci.patriciaM12.domain.exceptions.ReportNotFoundException if no report
     *         exists with the given {@code reportId}
     */
    ReportRequest findById(UUID reportId, UUID requestedBy);
}
