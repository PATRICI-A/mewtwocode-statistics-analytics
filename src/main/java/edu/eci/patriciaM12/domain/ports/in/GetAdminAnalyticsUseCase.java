package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.enums.MetricType;

import java.time.LocalDate;
import java.util.List;

/**
 * Input port (primary port) that defines the use cases available to retrieve
 * administrator-level analytics data.  Implementations are provided by the
 * application layer and invoked from web adapters or scheduled jobs.
 */
public interface GetAdminAnalyticsUseCase {

    /**
     * Retrieves the most recent {@link AdminAnalyticsSnapshot} for the given date.
     *
     * @param date the calendar date for which the snapshot is requested
     * @return the snapshot recorded on {@code date}
     * @throws edu.eci.patriciaM12.domain.exceptions.MetricNotFoundException if no snapshot
     *         exists for the specified date
     */
    AdminAnalyticsSnapshot getLatest(LocalDate date);

    /**
     * Retrieves all {@link AdminAnalyticsSnapshot} records within a date range,
     * ordered chronologically.
     *
     * @param dateFrom inclusive start date of the range
     * @param dateTo   inclusive end date of the range
     * @return list of snapshots within the specified range; may be empty if none exist
     */
    List<AdminAnalyticsSnapshot> getHistorical(LocalDate dateFrom, LocalDate dateTo);

    /**
     * Builds an {@link AdminAnalyticsResponse} summarising the requested metric type
     * for the given date range.  Used to populate the admin analytics panel in the UI.
     *
     * @param startDate  inclusive start date of the reporting window
     * @param endDate    inclusive end date of the reporting window
     * @param metricType the dimension of data to aggregate (e.g., USERS, PARCHES)
     * @return a populated {@link AdminAnalyticsResponse} for the given parameters
     */
    AdminAnalyticsResponse getPanel(LocalDate startDate, LocalDate endDate, MetricType metricType);
}
