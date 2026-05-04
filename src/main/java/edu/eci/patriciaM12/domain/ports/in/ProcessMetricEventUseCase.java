package edu.eci.patriciaM12.domain.ports.in;

import edu.eci.patriciaM12.domain.model.MetricEvent;

public interface ProcessMetricEventUseCase {
    void process(MetricEvent event);
}