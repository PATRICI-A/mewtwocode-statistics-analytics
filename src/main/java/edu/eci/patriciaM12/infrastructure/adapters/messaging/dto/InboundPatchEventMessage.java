package edu.eci.patriciaM12.infrastructure.adapters.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inbound Kafka message DTO used to deserialize patch interaction events published by
 * M06 (Feed & Search) on the {@code m06-patch-events} topic.
 * <p>
 * The field names match the JSON produced by {@code PatchEventMessage} in M06 exactly.
 * Unknown fields are silently ignored ({@code @JsonIgnoreProperties}) so that M06 can add
 * new optional fields without breaking M12's consumer.
 * </p>
 *
 * <p><strong>Topic:</strong> {@code m06-patch-events}<br>
 * <strong>Producer:</strong> M06 Feed & Search<br>
 * <strong>Consumer:</strong> M12 Statistics & Analytics (this class)</p>
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InboundPatchEventMessage {

    /**
     * Unique identifier of the event — used for idempotency checks.
     * If the same {@code eventId} has already been processed, it should be skipped.
     */
    private UUID eventId;

    /** Name of the originating module, e.g. {@code "M06"}. */
    private String sourceModule;

    /**
     * Type of interaction.  Valid values: {@code "JOIN"}, {@code "VIEW"}, {@code "SKIP"},
     * {@code "CREATE"}, {@code "STATUS_CHANGED"}.
     */
    private String eventType;

    /** UUID of the user who performed the action. */
    private UUID userId;

    /** UUID of the patch involved in the action. */
    private UUID patchId;

    /**
     * Thematic category of the patch at event time.
     * Matches {@link PatchCategory} values: STUDY, SPORTS, CULTURE, GAMING, FOOD, MUSIC, OTHER.
     */
    private PatchCategory patchCategory;

    /**
     * Campus zone identifier where the patch takes place.
     * {@code null} when not provided by M06 (e.g., for VIEW or SKIP events).
     */
    private String campusZone;

    /** Timestamp at which M06 emitted this event. */
    private LocalDateTime emittedAt;
}
