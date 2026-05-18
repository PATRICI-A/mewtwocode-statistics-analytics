package edu.eci.patriciaM12.infrastructure.external;

import edu.eci.patriciaM12.infrastructure.external.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "profile-service",
        url = "${services.profile.url}"
)
public interface ProfileFeignClient {

    @GetMapping("/internal/profiles/{userId}")
    UserProfileResponse getProfile(@PathVariable UUID userId);
}
