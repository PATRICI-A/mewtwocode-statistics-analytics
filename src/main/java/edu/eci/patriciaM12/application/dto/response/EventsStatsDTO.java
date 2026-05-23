package edu.eci.patriciaM12.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Aggregates institutional-level statistics about parche/event lifecycle states (RF-40 RN-40.2).
 * <p>
 * Returned as part of {@link InstitutionalStatsResponse} and populated by
 * {@code InstitutionalStatsService} from data sourced via {@code AdminSnapshotRepositoryPort}.
 * </p>
 */
@Value
@Builder
@Schema(
        name = "EventsStats",
        description = """
                Aggregated institutional-level statistics about parche/event lifecycle states (RF-40 RN-40.2). \
                Provides a complete view of event health across the campus."""
)
public class EventsStatsDTO {

    @Schema(
            description = "Total number of parches created within the observed period",
            example = "156"
    )
    Integer totalCreated;

    @Schema(
            description = "Number of parches currently in an active state",
            example = "89"
    )
    Integer activeCount;

    @Schema(
            description = "Number of parches that were cancelled before taking place",
            example = "23"
    )
    Integer cancelledCount;

    @Schema(
            description = "Number of parches that have already concluded",
            example = "44"
    )
    Integer finishedCount;
}