package edu.eci.patriciaM12.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class KongAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            authenticate(authHeader.substring(7));
        } catch (Exception e) {
            log.warn("Invalid JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) throws IOException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) return;

        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        JsonNode claims = new ObjectMapper().readTree(new String(decoded, StandardCharsets.UTF_8));

        String userIdStr = claims.has("sub") ? claims.get("sub").asText() : null;
        if (userIdStr == null) return;

        UUID userId = parseUserId(userIdStr);
        if (userId == null) return;

        String role = claims.has("role") ? claims.get("role").asText() : "USER";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, authorities));
    }

    private UUID parseUserId(String userIdStr) {
        try {
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("JWT sub is not a valid UUID: {}", userIdStr);
            return null;
        }
    }
}
