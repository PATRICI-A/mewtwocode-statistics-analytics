package edu.eci.patriciaM12.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**"
    };

    @Bean
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
