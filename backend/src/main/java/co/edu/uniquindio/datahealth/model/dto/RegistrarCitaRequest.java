package co.edu.uniquindio.datahealth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** No lleva id_eps ni id_medico: salen del JWT (ADR-03). */
public record RegistrarCitaRequest(
        @NotBlank(message = "Escribe el diagnóstico.")
        @Size(max = 2000, message = "El diagnóstico es demasiado largo (máximo 2000 caracteres).")
        String diagnostico,

        @Size(max = 2000, message = "Las observaciones son demasiado largas (máximo 2000 caracteres).")
        String observaciones) {
}