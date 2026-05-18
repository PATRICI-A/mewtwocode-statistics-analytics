package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import lombok.Builder;
import lombok.Value;

/**
 * Immutable value object that holds the aggregated statistics for a single
 * {@link PatchCategory} within a given analytics period.  Used inside
 * {@link AdminAnalyticsSnapshot#getTopCategories()} to rank categories by activity.
 */
@Value
@Builder
public class CategoryStat {

    /** The patch category these statistics refer to. */
    PatchCategory category;

    /** Total number of patch events recorded for this category. */
    int count;

    /** Proportion of this category's events relative to the overall total (0.0–1.0). */
    float percentage;
}