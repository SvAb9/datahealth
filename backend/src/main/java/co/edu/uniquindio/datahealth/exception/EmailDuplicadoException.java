package co.edu.uniquindio.datahealth.exception;

public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException() {
        super("Ya existe un usuario registrado con ese correo.");
    }
}
