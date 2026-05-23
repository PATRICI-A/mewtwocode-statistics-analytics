package edu.eci.patriciaM12.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration for the M12 statistics and analytics service.
 * Registers the {@link OpenAPI} bean that populates the Swagger UI with service metadata,
 * security schemes, and organised API tags.
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    /**
     * Produces the {@link OpenAPI} bean used by SpringDoc to generate the API specification.
     *
     * @return an {@link OpenAPI} instance configured with service metadata, security scheme,
     *         and organised tags for the Swagger UI
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("M12 - Statistics and Analytics Service API")
                        .description("""
                                This microservice is responsible for providing comprehensive analytics, metrics,
                                and reporting capabilities for the PATRICI.A platform at Universidad ECI.

                                Key responsibilities include:
                                - **Admin Analytics**: Global platform metrics including active users, parche statistics,
                                  event rankings, match success rates, campus heatmaps, retention figures, and automated
                                  alerts for significant metric drops (RF-18)
                                - **Student Dashboards**: Personal activity metrics, social indicators, and interaction
                                  analytics for authenticated students (RF-38, RF-39)
                                - **Institutional Statistics**: Campus-wide aggregated statistics for Bienestar users,
                                  including event lifecycle metrics, participation figures, and a composite social
                                  activity index (RF-40)
                                - **CSV Report Generation**: Asynchronous report generation with preview capability,
                                  download polling, and 30-day report history for administrators (RF-19)

                                All analytics endpoints aggregate data from the platform's interaction and event stores,
                                applying time-decay formulas, normalisation algorithms, and configurable date ranges.
                                Write operations (report generation) follow an async request-poll pattern.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("PATRICI.A Platform Team")
                                .email("support@eci.edu.co")
                                .url("https://www.escuelaing.edu.co")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        JWT Bearer token authentication for all analytics endpoints.
                                        Required roles:
                                        - `ESTUDIANTE` → Access to `/dashboard`, `/social-indicators`, `/interaction-analytics`
                                        - `ADMINISTRADOR` → Access to `/analytics/admin` and `/reports`
                                        - `BIENESTAR` → Access to `/analytics/institutional`
                                        """)))
                .addTagsItem(new Tag()
                        .name("Admin Analytics")
                        .description("""
                                Endpoints exclusively for users with the `ADMINISTRADOR` role (RF-18).
                                Provides the global analytics panel with aggregated platform metrics including:
                                active users time-series, parche statistics, event rankings, match success rates,
                                campus heatmaps, retention figures, and automated alerts for significant metric drops.
                                All data is scoped to configurable date ranges and supports filtering by metric category and/or faculty.
                                """))
                .addTagsItem(new Tag()
                        .name("Dashboard")
                        .description("""
                                Personal analytics endpoints for authenticated students (RF-38, RF-39).
                                Provides three dashboards:
                                - **Personal dashboard**: Activity metrics including parche participation, event RSVPs,
                                  and social connections (Empty Object Pattern — always returns HTTP 200)
                                - **Social indicators**: Weekly social engagement metrics with activity level classification
                                - **Interaction analytics**: Aggregated interaction data including most-active zone and peak day
                                All endpoints resolve student identity directly from the JWT token; no explicit user ID required.
                                """))
                .addTagsItem(new Tag()
                        .name("Institutional Statistics")
                        .description("""
                                Endpoints exclusively for users with the `BIENESTAR` role (RF-40).
                                Provides campus-wide aggregated statistics including:
                                - Event lifecycle metrics (creation, publication, cancellation, completion)
                                - Student participation and RSVP figures
                                - Composite social activity index calculated as: parche_attendance×0.40 + connections×0.35 + event_rsvp×0.25
                                This endpoint is always protected — the `BIENESTAR` JWT role is required even in the `dev` Spring profile.
                                """))
                .addTagsItem(new Tag()
                        .name("Reports")
                        .description("""
                                Manages asynchronous CSV report generation for users with the `ADMINISTRADOR` role (RF-19).
                                Follows an async request-poll pattern:
                                1. `POST /reports` → Queues generation, returns HTTP 202 with PENDING status and report ID
                                2. `GET /reports/{id}/download` → Poll until status becomes READY or FAILED
                                3. `GET /reports/history` → Lists all READY reports from the last 30 days
                                Supports preview mode (`preview: true`) for synchronous 10-row previews without file persistence (RN-19.4),
                                and scheduled recurring delivery with configurable frequency (RN-19.6).
                                """));
    }
}