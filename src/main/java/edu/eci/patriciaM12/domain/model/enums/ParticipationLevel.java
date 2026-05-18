package edu.eci.patriciaM12.domain.model.enums;

/**
 * Represents the engagement tier of a student on the patch platform, derived from
 * the total number of patches they have attended.
 *
 * <ul>
 *   <li>{@code NUEVO}     — fewer than 3 patches attended</li>
 *   <li>{@code ACTIVO}    — 3 to 9 patches attended</li>
 *   <li>{@code CONECTOR}  — 10 to 19 patches attended</li>
 *   <li>{@code EMBAJADOR} — 20 or more patches attended</li>
 * </ul>
 */
public enum ParticipationLevel {

    /** New participant: fewer than 3 patches attended. */
    NUEVO,

    /** Active participant: 3 to 9 patches attended. */
    ACTIVO,

    /** Connector: 10 to 19 patches attended. */
    CONECTOR,

    /** Ambassador: 20 or more patches attended. */
    EMBAJADOR
}