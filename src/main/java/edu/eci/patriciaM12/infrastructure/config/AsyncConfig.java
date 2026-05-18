package edu.eci.patriciaM12.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configures the Spring async task executor used by the report generation pipeline (RF-19).
 * <p>
 * Enabling {@code @Async} support here allows {@code ReportService#generateAsync} to run
 * in a dedicated thread pool, keeping the HTTP request thread free to return the initial
 * {@code 202 ACCEPTED} response immediately.
 * </p>
 *
 * <p>Thread pool sizing:
 * <ul>
 *   <li>Core pool: 4 threads — handles concurrent report requests under normal load</li>
 *   <li>Max pool: 8 threads — handles bursts without unbounded thread creation</li>
 *   <li>Queue capacity: 50 — buffers requests when all threads are busy</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Creates and configures the thread pool executor used for async report generation.
     * The bean name {@code "reportExecutor"} can be referenced in {@code @Async("reportExecutor")}
     * annotations to target this specific pool.
     *
     * @return the configured {@link ThreadPoolTaskExecutor}
     */
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("report-gen-");
        executor.initialize();
        return executor;
    }
}
