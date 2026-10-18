package co.edu.uniquindio.datahealth.model.dto;

/** Espejo de Paciente en el frontend (pacientes.models.ts). "eps" es el nombre de la entidad. */
public record PacienteResponse(Long idPaciente, String documento, String nombre, Integer edad,
                               String telefono, String direccion, String eps) {
}
