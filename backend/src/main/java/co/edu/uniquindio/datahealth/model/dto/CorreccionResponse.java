package co.edu.uniquindio.datahealth.model.dto;

import java.time.LocalDateTime;

public record CorreccionResponse(String motivo, String usuario, LocalDateTime fechaHora) {
}
