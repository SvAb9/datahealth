package co.edu.uniquindio.datahealth.exception;

public class CuentaBloqueadaException extends RuntimeException {

    public CuentaBloqueadaException(long minutos) {
        super("La cuenta está bloqueada temporalmente por intentos fallidos. Intenta de nuevo en "
                + minutos + (minutos == 1 ? " minuto." : " minutos."));
    }
}