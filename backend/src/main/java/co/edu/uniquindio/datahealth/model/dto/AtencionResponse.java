package co.edu.uniquindio.datahealth.model.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Espejo de Atencion en el frontend (historia-clinica.models.ts). estado: ABIERTA | CERRADA. */
public record AtencionResponse(Long idCita, LocalDateTime fecha, String estado, String medico,
                               String diagnostico, List<OrdenResponse> ordenes,
                               List<CorreccionResponse> correcciones) {
}
