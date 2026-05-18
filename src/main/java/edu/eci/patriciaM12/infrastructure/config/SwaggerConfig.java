package edu.eci.patriciaM12.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration for the M12 statistics and analytics service.
 * Registers the {@link OpenAPI} bean that populates the Swagger UI with the service title,
 * description, and version metadata.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Produces the {@link OpenAPI} bean used by SpringDoc to generate the API specification.
     *
     * @return an {@link OpenAPI} instance configured with the service title, description, and version
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("M12 - Statistics and Analytics")
                        .description("Metrics and reporting service for PATRICI.A")
                        .version("0.0.1-SNAPSHOT"));
    }
}
