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
 * Infrastructure adapter that implements {@link CsvGeneratorPort} using OpenCSV 5.9.
 * <p>
 * Generates a CSV file on the local filesystem under the directory configured via
 * {@code m12.reports.output-dir} (defaults to {@code /tmp/reports}).
 * The output file name includes a timestamp with second precision to avoid collisions.
 * </p>
 */
@Component
public class CsvGeneratorAdapter implements CsvGeneratorPort {

    @Value("${m12.reports.output-dir:/tmp/reports}")
    private String outputDir;

    /**
     * Generates a CSV file from the supplied report filters and writes it to the output directory.
     * The CSV contains one header row and one data row with the filter values used.
     * Enum fields default to {@code "ALL"} when {@code null}.
     *
     * @param filters the report filter criteria to embed in the CSV
     * @return the absolute file-system path of the generated CSV file
     * @throws edu.eci.patriciaM12.domain.exceptions.CsvGenerationException if an I/O error occurs
     *         during directory creation or file writing
     */
    @Override
    public String generate(ReportFilters filters) {
        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);

            String fileName = "report_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
            Path file = dir.resolve(fileName);

            try (CSVWriter writer = new CSVWriter(new FileWriter(file.toFile()))) {
                writer.writeNext(new String[]{"dateFrom", "dateTo", "category", "campusZone"});
                writer.writeNext(new String[]{
                        filters.getDateFrom().toString(),
                        filters.getDateTo().toString(),
                        filters.getCategory()   != null ? filters.getCategory().name()   : "ALL",
                        filters.getCampusZone() != null ? filters.getCampusZone().name() : "ALL"
                });
            }

            return file.toString();
        } catch (Exception e) {
            throw new CsvGenerationException("Error generando reporte CSV: " + e.getMessage());
        }
    }
}