package edu.eci.patriciaM12.infrastructure.external.dto;

import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String name,
        String email,
        String academicProgram,
        Integer semester) {
}
