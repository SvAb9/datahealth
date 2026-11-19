package co.edu.uniquindio.datahealth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarResultadoRequest(
        @NotBlank(message = "Escribe el resultado.")
        @Size(max = 2000, message = "El resultado es demasiado largo (máximo 2000 caracteres).")
        String resultado) {
}