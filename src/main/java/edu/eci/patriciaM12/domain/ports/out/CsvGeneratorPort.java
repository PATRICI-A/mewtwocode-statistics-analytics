package edu.eci.patriciaM12.domain.ports.out;

import edu.eci.patriciaM12.domain.model.ReportFilters;

public interface CsvGeneratorPort {
    String generate(ReportFilters filters);
}