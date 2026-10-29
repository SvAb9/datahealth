package co.edu.uniquindio.datahealth.exception;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import javax.naming.AuthenticationException;

import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> credencialesInvalidas(CredencialesInvalidasException ex, HttpServletRequest req) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ApiError> cuentaBloqueada(CuentaBloqueadaException ex, HttpServletRequest req) {
        return responder(HttpStatus.LOCKED, ex.getMessage(), req);
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ApiError> emailDuplicado(EmailDuplicadoException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    public ResponseEntity<ApiError> rolNoPermitido(RolNoPermitidoException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /** 404: el paciente no existe o es de otra EPS (RN-01). */
    @ExceptionHandler(PacienteNoEncontradoException.class)
    public ResponseEntity<ApiError> pacienteNoEncontrado(PacienteNoEncontradoException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(PacienteDuplicadoException.class)
    public ResponseEntity<ApiError> pacienteDuplicado(PacienteDuplicadoException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ApiError> datosInvalidos(DatosInvalidosException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> datosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getDefaultMessage())
                .orElse("Los datos enviados no son válidos.");
        return responder(HttpStatus.BAD_REQUEST, mensaje, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "La solicitud no tiene el formato esperado.", req);
    }

    private ResponseEntity<ApiError> responder(HttpStatus estado, String mensaje, HttpServletRequest req) {
        ApiError cuerpo = new ApiError(LocalDateTime.now(), estado.value(), mensaje, req.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> errorInesperado(Exception ex, HttpServletRequest req) throws Exception {
        if (ex instanceof AccessDeniedException || ex instanceof AuthenticationException
                || ex instanceof ServletException || ex instanceof TypeMismatchException
                || ex instanceof ErrorResponse) {
            throw ex; // los resuelve Spring Security / MVC con su propio código (401, 403, 400, 404, 405)
        }
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intenta de nuevo más tarde.", req);
    }

}