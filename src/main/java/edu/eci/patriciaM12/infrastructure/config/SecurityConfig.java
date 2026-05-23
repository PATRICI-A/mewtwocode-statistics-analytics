package edu.eci.patriciaM12.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the M12 statistics and analytics service.
 * <p>
 * Kong (API Gateway) handles all JWT validation. This service trusts Kong and only
 * decodes the JWT payload (via {@link KongAuthFilter}) to extract the authenticated
 * user ID and role — no signature re-validation is performed.
 * </p>
 * Two filter chains are registered:
 * <ul>
 *   <li>A <em>dev-only</em> chain (order 1) that permits all requests to the admin analytics
 *       endpoints without authentication, simplifying local development.</li>
 *   <li>A <em>production</em> chain (order 2) that requires the {@code ADMINISTRADOR} role for
 *       admin endpoints, allows public access to Swagger UI paths, and mandates authentication
 *       for every other route.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**"
    };

    private static final String ADMIN_PATH = "/api/v1/analytics/admin/**";
    private static final String INSTITUTIONAL_PATH = "/api/v1/analytics/institutional/**";

    @Bean
    @Profile("dev")
    @Order(1)
    public SecurityFilterChain devAdminAnalyticsFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(ADMIN_PATH, INSTITUTIONAL_PATH)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           KongAuthFilter kongAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers(ADMIN_PATH).hasRole("ADMINISTRADOR")
                        .requestMatchers(INSTITUTIONAL_PATH).hasAnyRole("ADMINISTRADOR", "BIENESTAR")
                        .anyRequest().authenticated())
                .addFilterBefore(kongAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
