package co.edu.uniquindio.datahealth.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        // Mensaje único: no revela si falló el correo o la contraseña (CA-02).
        super("Correo o contraseña incorrectos.");
    }
}