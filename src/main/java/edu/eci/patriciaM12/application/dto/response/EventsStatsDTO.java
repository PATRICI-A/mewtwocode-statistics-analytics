package edu.eci.patriciaM12.application.dto.response;

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
public class EventsStatsDTO {

    /** Total number of parches created within the observed period. */
    Integer totalCreated;

    /** Number of parches currently in an active state. */
    Integer activeCount;

    /** Number of parches that were cancelled before taking place. */
    Integer cancelledCount;

    /** Number of parches that have already concluded. */
    Integer finishedCount;
}
