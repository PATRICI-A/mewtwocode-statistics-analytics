package edu.eci.patriciaM12.application.dto.response;

import edu.eci.patriciaM12.domain.model.enums.CampusZone;
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
public class HeatmapDTO {

    /**
     * Outer key: campus zone identifier.
     * Inner key: hour of day (0–23, UTC-5 Bogotá time).
     * Inner value: number of activity events recorded at that zone and hour.
     */
    Map<CampusZone, Map<Integer, Integer>> zones;

    /**
     * Campus zone that recorded the highest cumulative activity count across all hours.
     * {@code null} when no data is available.
     */
    CampusZone peakZone;

    /**
     * Hour of day (0–23) that recorded the highest cumulative activity across all zones.
     * {@code null} when no data is available.
     */
    Integer peakHour;
}
