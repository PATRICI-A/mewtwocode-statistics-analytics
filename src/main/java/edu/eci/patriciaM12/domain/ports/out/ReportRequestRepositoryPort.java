package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.ReportRequest;

import java.util.Optional;
import java.util.UUID;

public interface ReportRequestRepositoryPort {
    ReportRequest save(ReportRequest reportRequest);
    Optional<ReportRequest> findById(UUID id);
}