package co.edu.uniquindio.datahealth.model.dto;

import java.time.LocalDateTime;

public record AuditoriaResponse(Long id, String usuario, String accion, String detalle, LocalDateTime fechaHora) {
}