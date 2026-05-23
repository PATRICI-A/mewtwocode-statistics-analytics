package edu.eci.patriciaM12.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "profile-service",
        url = "${services.profile.url}"
)
public interface ProfileFeignClient {

    @GetMapping("/api/v1/users/internal/{userId}/friends-count")
    Integer getUserFriendsCount(@PathVariable UUID userId);
}
