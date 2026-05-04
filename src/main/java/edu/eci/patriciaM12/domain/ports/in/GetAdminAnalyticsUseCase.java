package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.domain.model.AdminAnalyticsSnapshot;

import java.time.LocalDate;
import java.util.List;

public interface GetAdminAnalyticsUseCase {
    AdminAnalyticsSnapshot getLatest(LocalDate date);
    List<AdminAnalyticsSnapshot> getHistorical(LocalDate dateFrom, LocalDate dateTo);
}