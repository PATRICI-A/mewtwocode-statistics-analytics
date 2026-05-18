package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.enums.MetricType;

import java.time.LocalDate;
import java.util.List;

/**
 * Input port (primary port) that defines the use cases available to retrieve
 * administrator-level analytics data (RF-18).  Implementations are provided by the
 * application layer and invoked from web adapters or scheduled jobs.
 */
public interface GetAdminAnalyticsUseCase {

    /**
     * Retrieves the most recent {@link AdminAnalyticsSnapshot} for the given date.
     *
     * @param date the calendar date for which the snapshot is requested
     * @return the snapshot recorded on {@code date}, or {@code null} if none exists
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
     * for the given date range and optional faculty filter (RF-18 RN-18.10).
     * <p>
     * Used to populate the admin analytics panel in the UI.  When {@code startDate} or
     * {@code endDate} are {@code null} the active academic semester boundaries are used.
     * When {@code metricType} is {@code null} all metrics are included.
     * When {@code facultyFilter} is {@code null} no faculty restriction is applied.
     * </p>
     *
     * @param startDate     inclusive start date of the reporting window, or {@code null} for semester default
     * @param endDate       inclusive end date of the reporting window, or {@code null} for semester default
     * @param metricType    the dimension to aggregate, or {@code null} to include all metrics
     * @param facultyFilter optional faculty name to restrict the analytics scope; {@code null} means all faculties
     * @return a populated {@link AdminAnalyticsResponse} for the given parameters
     */
    AdminAnalyticsResponse getPanel(LocalDate startDate, LocalDate endDate, MetricType metricType, String facultyFilter);
}
