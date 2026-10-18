package co.edu.uniquindio.datahealth.exception;

public class RolNoPermitidoException extends RuntimeException {

    public RolNoPermitidoException() {
        super("El rol elegido no se puede asignar desde esta pantalla.");
    }
}
