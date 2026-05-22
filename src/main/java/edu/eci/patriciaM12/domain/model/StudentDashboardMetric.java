package edu.eci.patriciaM12.domain.model;

import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import edu.eci.patriciaM12.domain.model.enums.ParticipationLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregated metric record for a single student's activity on the patch platform
 * during a specific period.  Exposes derived metrics such as participation level
 * and staleness to support the student-facing analytics dashboard.
 *
 * <p>Instances are built and cached by the application layer and are considered
 * stale after 5 minutes from {@link #getComputedAt()}.</p>
 */
@Getter
@Builder
public class StudentDashboardMetric {

    /** Unique identifier of the student these metrics belong to. */
    private UUID userId;

    /** The calendar date that defines the analytics period for this record. */
    private LocalDate period;

    /** Total number of patches the student has attended during the period. */
    private int patchesAttended;

    /** The patch category the student has participated in most frequently. */
    private PatchCategory topCategory;

    /**
     * Distribution of the student's patch activity across the days of the week.
     * Key: day of the week; value: number of patches attended on that day.
     */
    private Map<DayOfWeek, Integer> weeklyActivity;

    /** Timestamp at which this metric record was last computed and stored. */
    private LocalDateTime computedAt;

    /**
     * All badges earned by the student, fetched live from GamificationService (PTR17).
     * May be {@code null} or empty when the gamification service is unavailable.
     */
    private List<AchievementInfo> earnedBadges;

    /**
     * Fraction of monas collected toward the next gamification level (0.0–1.0).
     * Computed from GamificationService level data (PTR17).
     * Returns {@code 1.0} when the student is already at the maximum level.
     */
    private double progressToNextLevel;

    /**
     * Determines whether this metric record is outdated and should be recomputed.
     * A record is considered stale when it was computed more than 5 minutes ago.
     *
     * @return {@code true} if the record was computed more than 5 minutes ago;
     *         {@code false} if the record is still fresh or {@code computedAt} is {@code null}
     */
    public boolean isStale() {
        return computedAt != null &&
                computedAt.isBefore(LocalDateTime.now().minusMinutes(5));
    }

    /**
     * Derives the student's participation level from the total number of patches attended,
     * applying the platform's engagement tier thresholds.
     *
     * <ul>
     *   <li>{@link ParticipationLevel#EMBAJADOR} — 20 or more patches</li>
     *   <li>{@link ParticipationLevel#CONECTOR}  — 10 to 19 patches</li>
     *   <li>{@link ParticipationLevel#ACTIVO}    — 3 to 9 patches</li>
     *   <li>{@link ParticipationLevel#NUEVO}     — fewer than 3 patches</li>
     * </ul>
     *
     * @return the {@link ParticipationLevel} corresponding to {@link #getPatchesAttended()}
     */
    public ParticipationLevel getParticipationLevel() {
        if (patchesAttended >= 20) return ParticipationLevel.EMBAJADOR;
        if (patchesAttended >= 10) return ParticipationLevel.CONECTOR;
        if (patchesAttended >= 3)  return ParticipationLevel.ACTIVO;
        return ParticipationLevel.NUEVO;
    }

}