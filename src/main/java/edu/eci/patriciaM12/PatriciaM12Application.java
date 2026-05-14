package edu.eci.patriciaM12;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the statistics and analytics microservice (M12).
 * Bootstraps the Spring Boot application context, enabling auto-configuration,
 * component scanning, and Kafka consumer registration.
 */
@SpringBootApplication
public class PatriciaM12Application {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application at startup
	 */
	public static void main(String[] args) {
		SpringApplication.run(PatriciaM12Application.class, args);
	}

}
