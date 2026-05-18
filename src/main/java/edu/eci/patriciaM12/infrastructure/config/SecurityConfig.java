package edu.eci.patriciaM12.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Spring Security configuration for the M12 statistics and analytics service.
 * <p>
 * Configures a stateless, JWT-based OAuth2 resource server.  Two filter chains are registered:
 * <ul>
 *   <li>A <em>dev-only</em> chain (order 1) that permits all requests to the admin analytics
 *       endpoints without authentication, simplifying local development.</li>
 *   <li>A <em>production</em> chain (order 2) that requires the {@code ADMINISTRADOR} role for
 *       admin endpoints, allows public access to Swagger UI paths, and mandates authentication
 *       for every other route.</li>
 * </ul>
 * JWT claims are mapped to Spring Security granted authorities by reading the {@code role}
 * (single-value) and {@code roles} (list) claims and prefixing them with {@code ROLE_} when
 * the prefix is not already present.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**"
    };

    private static final String ADMIN_PATH = "/api/v1/analytics/admin/**";
    private static final String INSTITUTIONAL_PATH = "/api/v1/analytics/institutional/**";

    /**
     * Registers permissive security filter chains for the admin and institutional analytics routes,
     * active only in the {@code dev} Spring profile.  All requests to those paths are permitted
     * without authentication, simplifying local development.
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
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

    /**
     * Registers the primary security filter chain used in all profiles.
     * Disables CSRF (stateless API), enforces JWT authentication, allows Swagger UI paths publicly,
     * and restricts admin analytics endpoints to users with the {@code ADMINISTRADOR} role.
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers(ADMIN_PATH).hasRole("ADMINISTRADOR")
                        .requestMatchers(INSTITUTIONAL_PATH).hasAnyRole("ADMINISTRADOR", "BIENESTAR")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Produces a {@link JwtAuthenticationConverter} that extracts granted authorities from the
     * JWT by combining the singular {@code role} claim and the list {@code roles} claim.
     *
     * @return a configured {@link JwtAuthenticationConverter}
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> Stream
                .concat(authoritiesFromClaim(jwt.getClaimAsString("role")).stream(),
                        authoritiesFromClaims(jwt.getClaimAsStringList("roles")).stream())
                .toList());
        return converter;
    }

    /**
     * Converts a single role string from a JWT claim into a list of {@link GrantedAuthority} objects.
     * Returns an empty list if the role is {@code null} or blank.
     *
     * @param role the role string extracted from the {@code role} JWT claim
     * @return a single-element list containing the corresponding authority, or an empty list
     */
    private Collection<GrantedAuthority> authoritiesFromClaim(String role) {
        if (role == null || role.isBlank()) {
            return List.of();
        }
        return List.of(toAuthority(role));
    }

    /**
     * Converts a list of role strings from a JWT claim into a list of {@link GrantedAuthority} objects.
     * Null or blank entries within the list are filtered out.
     * Returns an empty list if {@code roles} itself is {@code null}.
     *
     * @param roles the list of role strings extracted from the {@code roles} JWT claim
     * @return a list of corresponding authorities; never {@code null}
     */
    private Collection<GrantedAuthority> authoritiesFromClaims(List<String> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(this::toAuthority)
                .toList();
    }

    /**
     * Converts a raw role string into a Spring Security {@link GrantedAuthority}.
     * Prepends the {@code ROLE_} prefix if it is not already present.
     *
     * @param role the raw role string (e.g. {@code "ADMINISTRADOR"} or {@code "ROLE_ADMINISTRADOR"})
     * @return a {@link SimpleGrantedAuthority} with the normalised role name
     */
    private GrantedAuthority toAuthority(String role) {
        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return new SimpleGrantedAuthority(normalizedRole);
    }
}
