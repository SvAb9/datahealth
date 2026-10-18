package co.edu.uniquindio.datahealth.exception;

/**
 * 404. Se usa igual cuando el paciente no existe y cuando pertenece a otra EPS,
 * para no revelar su existencia (RN-01).
 */
public class PacienteNoEncontradoException extends RuntimeException {

    public PacienteNoEncontradoException() {
        super("No encontramos un paciente con esos datos.");
    }

    public PacienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
