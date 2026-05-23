package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.CampusZone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * Data transfer object representing campus activity heatmap data (RF-18 RN-18.4).
 * <p>
 * Visualises the spatial distribution of student participation across campus zones.
 * Each zone maps to an inner map of hour-of-day (0–23) to activity count, allowing
 * the frontend to render a 2-D heatmap (zone × hour).
 * </p>
 */
@Value
@Builder
@Schema(
        name = "Heatmap",
        description = """
                Campus activity heatmap data (RF-18 RN-18.4). Visualises the spatial and temporal \
                distribution of student participation across campus zones. Each zone contains an \
                hour-by-hour breakdown of activity counts, enabling 2-D heatmap visualisation."""
)
public class HeatmapDTO {

    @Schema(
            description = """
                    Outer key: campus zone identifier. Inner key: hour of day (0–23, UTC-5 Bogotá time). \
                    Inner value: number of activity events recorded at that zone and hour.""",
            example = "{\"BIBLIOTECA\": {10: 45, 11: 52, 14: 38}, \"CAFETERIA\": {12: 78, 13: 65}}"
    )
    Map<CampusZone, Map<Integer, Integer>> zones;

    @Schema(
            description = "Campus zone that recorded the highest cumulative activity count across all hours",
            example = "BIBLIOTECA"
    )
    CampusZone peakZone;

    @Schema(
            description = "Hour of day (0–23) that recorded the highest cumulative activity across all zones",
            example = "12"
    )
    Integer peakHour;
}