package edu.eci.patriciaM12.infrastructure.adapters.persistence.entity;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    @Column(name = "filters", columnDefinition = "TEXT")
    private String filters;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}