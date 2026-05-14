package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.ReportFilters;

/**
 * Output port (secondary port) that abstracts the generation of CSV-formatted analytics
 * reports.  Infrastructure adapters implement this interface to produce the actual file
 * (e.g., writing to local storage or an object-storage bucket) and return a URL where
 * the file can be downloaded.
 */
public interface CsvGeneratorPort {

    /**
     * Generates a CSV report applying the given filter criteria and returns the URL
     * of the resulting file.
     *
     * @param filters the filter criteria that determine the data included in the report;
     *                must not be {@code null}
     * @return a non-null URL string pointing to the generated CSV file
     * @throws edu.eci.patriciaM12.domain.exceptions.CsvGenerationException if the file
     *         cannot be produced due to an I/O or data-formatting error
     */
    String generate(ReportFilters filters);
}