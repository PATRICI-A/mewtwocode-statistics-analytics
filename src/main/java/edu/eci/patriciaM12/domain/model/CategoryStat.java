package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryStat {
    PatchCategory category;
    int count;
    float percentage;
}