package co.edu.uniquindio.datahealth.model.dto;

import java.time.LocalDateTime;

public record ResultadoResponse(Long idOrden, String resultado, LocalDateTime fechaResultado) {
}