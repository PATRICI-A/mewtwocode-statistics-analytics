package edu.eci.patriciaM12.infrastructure.adapters.adapter;

import edu.eci.patriciaM12.domain.exceptions.CsvGenerationException;
import edu.eci.patriciaM12.domain.model.ReportFilters;
import edu.eci.patriciaM12.domain.ports.out.CsvGeneratorPort;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Infrastructure adapter that implements {@link CsvGeneratorPort} using OpenCSV 5.9 (RF-19).
 * <p>
 * Generates CSV files on the local filesystem under the directory configured via
 * {@code m12.reports.output-dir} (defaults to {@code /tmp/reports}).
 * The output file name includes a timestamp with second precision to avoid collisions.
 * </p>
 *
 * <p><strong>Microservice integration note:</strong> The current implementation writes
 * filter metadata only.  Once M06 (Feed & Search) publishes parche records via the
 * Kafka topic {@code m12-analytics-group}, this adapter will be extended to write one
 * row per parche record fetched from the {@code AdminSnapshotRepositoryPort}.</p>
 */
@Component
public class CsvGeneratorAdapter implements CsvGeneratorPort {

    private static final String[] HEADER = {"dateFrom", "dateTo", "category", "campusZone"};

    @Value("${m12.reports.output-dir:/tmp/reports}")
    private String outputDir;

    /**
     * Generates a full CSV report from the supplied filter criteria and writes it to the
     * output directory.  The file contains one header row and one data row per filter set.
     *
     * @param filters the report filter criteria to embed in the CSV; must not be {@code null}
     * @return the absolute filesystem path of the generated CSV file
     * @throws CsvGenerationException if an I/O error occurs during directory creation or file writing
     */
    @Override
    public String generate(ReportFilters filters) {
        return write(filters, "report_", Integer.MAX_VALUE);
    }

    /**
     * Generates a preview CSV containing at most {@code maxRows} data rows.
     * The file is written to the same output directory as full reports but is prefixed
     * with {@code preview_} to distinguish it.
     *
     * @param filters the report filter criteria; must not be {@code null}
     * @param maxRows the maximum number of data rows to write (typically 10)
     * @return the absolute filesystem path of the generated preview CSV file
     * @throws CsvGenerationException if an I/O error occurs during file writing
     */
    @Override
    public String generatePreview(ReportFilters filters, int maxRows) {
        return write(filters, "preview_", maxRows);
    }

    /**
     * Core write routine shared by {@link #generate} and {@link #generatePreview}.
     * Creates the output directory if absent, builds the file name with a timestamp,
     * and delegates to OpenCSV.
     *
     * @param filters    the filter criteria
     * @param prefix     file name prefix ({@code "report_"} or {@code "preview_"})
     * @param maxRows    maximum data rows to emit; {@link Integer#MAX_VALUE} means unlimited
     * @return the absolute path of the written file
     */
    private String write(ReportFilters filters, String prefix, int maxRows) {
        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Path file = dir.resolve(prefix + timestamp + ".csv");

            try (CSVWriter writer = new CSVWriter(new FileWriter(file.toFile()))) {
                writer.writeNext(HEADER);
                int rows = 0;
                if (rows < maxRows) {
                    writer.writeNext(new String[]{
                            filters.getDateFrom().toString(),
                            filters.getDateTo().toString(),
                            filters.getCategory() != null ? filters.getCategory().name() : "ALL",
                            filters.getCampusZone() != null ? filters.getCampusZone().name() : "ALL"
                    });
                }
            }

            return file.toString();
        } catch (Exception e) {
            throw new CsvGenerationException("Error generating CSV report: " + e.getMessage());
        }
    }
}
