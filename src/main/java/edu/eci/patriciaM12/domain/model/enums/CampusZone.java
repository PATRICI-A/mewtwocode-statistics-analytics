package edu.eci.patriciaM12.domain.model.enums;

/**
 * Represents the physical zones of the university campus where a patch activity
 * can take place.  The special value {@code EXTERNO} covers locations outside
 * the main campus perimeter.
 */
public enum CampusZone {

    /** University library area. */
    BIBLIOTECA,

    /** Cafeteria and dining area. */
    CAFETERIA,

    /** Sports court or playing field. */
    CANCHA,

    /** Classroom or lecture hall. */
    SALON,

    /** Parking lot area. */
    PARQUEADERO,

    /** Location outside the university campus. */
    EXTERNO
}