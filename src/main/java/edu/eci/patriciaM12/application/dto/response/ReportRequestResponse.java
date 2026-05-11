package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.ReportRequest;
import edu.eci.patriciaM12.domain.model.enums.ReportStatus;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ReportRequestResponse {

    UUID id;
    ReportStatus status;
    String fileUrl;

    public static ReportRequestResponse from(ReportRequest report) {
        return ReportRequestResponse.builder()
                .id(report.getId())
                .status(report.getStatus())
                .fileUrl(report.getFileUrl())
                .build();
    }
}