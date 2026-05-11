package edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.ReportRequestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReportRequestMapper {

    private final ObjectMapper objectMapper;

    public ReportRequest toDomain(ReportRequestEntity entity) {
        ReportFilters filters = null;
        try {
            if (entity.getFilters() != null) {
                filters = objectMapper.readValue(entity.getFilters(), ReportFilters.class);
            }
        } catch (Exception ignored) {}

        return ReportRequest.builder()
                .id(entity.getId())
                .requestedBy(entity.getRequestedBy())
                .dateFrom(entity.getDateFrom())
                .dateTo(entity.getDateTo())
                .filters(filters)
                .status(entity.getStatus())
                .fileUrl(entity.getFileUrl())
                .build();
    }

    public ReportRequestEntity toEntity(ReportRequest domain) {
        String filtersJson = null;
        try {
            filtersJson = objectMapper.writeValueAsString(domain.getFilters());
        } catch (Exception ignored) {}

        return ReportRequestEntity.builder()
                .id(domain.getId())
                .requestedBy(domain.getRequestedBy())
                .dateFrom(domain.getDateFrom())
                .dateTo(domain.getDateTo())
                .filters(filtersJson)
                .status(domain.getStatus())
                .fileUrl(domain.getFileUrl())
                .createdAt(LocalDateTime.now())
                .build();
    }
}