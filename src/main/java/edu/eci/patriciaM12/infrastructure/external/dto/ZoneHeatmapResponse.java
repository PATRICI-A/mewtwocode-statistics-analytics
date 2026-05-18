package edu.eci.patriciaM12.infrastructure.external.dto;

import java.util.List;

public record ZoneHeatmapResponse(List<ZoneEntry> zones) {

    public record ZoneEntry(
            String campusZone,
            int activeUsers,
            int activePatches,
            String peakHour) {}
}
