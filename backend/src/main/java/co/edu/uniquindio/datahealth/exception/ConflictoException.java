package co.edu.uniquindio.datahealth.exception;

/** 409: la operación choca con el estado actual (cita cerrada, resultado ya registrado). */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
