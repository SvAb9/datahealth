package co.edu.uniquindio.datahealth.model.dto;

/** tipo: MEDICA | MEDICAMENTO | LABORATORIO. */
public record OrdenResponse(Long idOrden, String tipo, String descripcion) {
}