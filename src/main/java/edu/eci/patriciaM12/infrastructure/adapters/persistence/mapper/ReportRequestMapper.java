package edu.eci.patriciaM12.infrastructure.adapters.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.infrastructure.adapters.persistence.entity.ReportRequestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Spring-managed mapper that converts between {@link ReportRequestEntity} (JPA layer)
 * and {@link ReportRequest} (domain layer).
 * The {@code filters} column is stored as a JSON string; this mapper uses Jackson's
 * {@link com.fasterxml.jackson.databind.ObjectMapper} for serialisation and deserialisation.
 * Deserialisation errors are silently ignored and the filters field is left {@code null}.
 */
@Component
@RequiredArgsConstructor
public class ReportRequestMapper {

    private final ObjectMapper objectMapper;

    /**
     * Converts a JPA entity into its corresponding domain model.
     * If the {@code filters} JSON cannot be parsed the returned domain object will have
     * a {@code null} filters field.
     *
     * @param entity the persistence entity to convert
     * @return the equivalent domain {@link ReportRequest}
     */
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

    /**
     * Converts a domain model into its corresponding JPA entity.
     * The {@code filters} object is serialised to JSON; serialisation errors are silently
     * ignored and the column is left {@code null}.  The {@code createdAt} timestamp is
     * always set to the current time.
     *
     * @param domain the domain report request to convert
     * @return the equivalent {@link ReportRequestEntity} ready for persistence
     */
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