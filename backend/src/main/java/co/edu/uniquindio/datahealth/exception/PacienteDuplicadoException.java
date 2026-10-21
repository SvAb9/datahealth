package co.edu.uniquindio.datahealth.exception;

public class PacienteDuplicadoException extends RuntimeException {

    public PacienteDuplicadoException() {
        super("Ya existe un paciente registrado con ese documento.");
    }

    public PacienteDuplicadoException(String mensaje) {
        super(mensaje);
    }
}