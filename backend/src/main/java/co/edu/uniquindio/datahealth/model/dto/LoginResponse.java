package co.edu.uniquindio.datahealth.model.dto;

/** El rol, el id_eps y el nombre viajan dentro del JWT (ADR-04). */
public record LoginResponse(String token) {
}