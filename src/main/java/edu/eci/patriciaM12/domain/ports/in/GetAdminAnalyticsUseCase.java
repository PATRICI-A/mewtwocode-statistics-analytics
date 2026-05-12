package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.application.dto.response.AdminAnalyticsResponse;
import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;
import edu.eci.patriciaM12.domain.model.enums.MetricType;

import java.time.LocalDate;
import java.util.List;

public interface GetAdminAnalyticsUseCase {
    AdminAnalyticsSnapshot getLatest(LocalDate date);
    List<AdminAnalyticsSnapshot> getHistorical(LocalDate dateFrom, LocalDate dateTo);
    AdminAnalyticsResponse getPanel(LocalDate startDate, LocalDate endDate, MetricType metricType);
}
