package co.edu.uniquindio.datahealth.exception;

import java.time.LocalDateTime;

/** Formato uniforme de error (ADR-07): fecha, estado, mensaje y ruta. */
public record ApiError(LocalDateTime fecha, int estado, String mensaje, String ruta) {
}