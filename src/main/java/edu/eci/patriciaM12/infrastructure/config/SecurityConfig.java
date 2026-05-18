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

    @Bean
    @Profile("dev")
    @Order(1)
    public SecurityFilterChain devAdminAnalyticsFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/analytics/admin/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers("/api/v1/analytics/admin/**").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> Stream
                .concat(authoritiesFromClaim(jwt.getClaimAsString("role")).stream(),
                        authoritiesFromClaims(jwt.getClaimAsStringList("roles")).stream())
                .toList());
        return converter;
    }

    private Collection<GrantedAuthority> authoritiesFromClaim(String role) {
        if (role == null || role.isBlank()) {
            return List.of();
        }
        return List.of(toAuthority(role));
    }

    private Collection<GrantedAuthority> authoritiesFromClaims(List<String> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(this::toAuthority)
                .toList();
    }

    private GrantedAuthority toAuthority(String role) {
        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return new SimpleGrantedAuthority(normalizedRole);
    }
}
