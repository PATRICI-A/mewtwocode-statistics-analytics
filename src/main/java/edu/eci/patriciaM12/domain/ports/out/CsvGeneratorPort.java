package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.ReportFilters;

/**
 * Output port (secondary port) that abstracts the generation of CSV-formatted analytics
 * reports (RF-19).  Infrastructure adapters implement this interface to produce the actual
 * file (e.g., writing to local storage or an object-storage bucket) and return a URL where
 * the file can be downloaded.
 */
public interface CsvGeneratorPort {

    /**
     * Generates a full CSV report applying the given filter criteria and returns the path
     * of the resulting file (RF-19).
     *
     * @param filters the filter criteria that determine the data included in the report;
     *                must not be {@code null}
     * @return a non-null path string pointing to the generated CSV file
     * @throws edu.eci.patriciaM12.domain.exceptions.CsvGenerationException if the file
     *         cannot be produced due to an I/O or data-formatting error
     */
    String generate(ReportFilters filters);

    /**
     * Generates a preview CSV containing at most {@code maxRows} data rows and returns
     * the path of the resulting file.  Unlike {@link #generate(ReportFilters)}, the
     * preview file is not intended for long-term storage (RF-19 RN-19.4).
     *
     * @param filters the filter criteria that determine the data included in the preview;
     *                must not be {@code null}
     * @param maxRows the maximum number of data rows to include (typically 10)
     * @return a non-null path string pointing to the generated preview CSV file
     * @throws edu.eci.patriciaM12.domain.exceptions.CsvGenerationException if the file
     *         cannot be produced due to an I/O or data-formatting error
     */
    String generatePreview(ReportFilters filters, int maxRows);
}
