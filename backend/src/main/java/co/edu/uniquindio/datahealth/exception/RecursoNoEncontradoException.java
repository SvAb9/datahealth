package co.edu.uniquindio.datahealth.exception;

/** 404 para citas u órdenes inexistentes o de otra EPS (RN-01). */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}