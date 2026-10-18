package co.edu.uniquindio.datahealth.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** No lleva id_eps: el backend lo toma del JWT (ADR-03, RF-10). */
public record RegistrarPacienteRequest(
        @NotBlank(message = "Escribe el documento de identidad.")
        @Pattern(regexp = "^\\d{5,15}$", message = "El documento debe tener solo números, entre 5 y 15 dígitos.")
        String documento,

        @NotBlank(message = "Escribe el nombre completo.")
        @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres.")
        String nombre,

        @NotNull(message = "Escribe la edad.")
        @Min(value = 0, message = "La edad debe estar entre 0 y 120.")
        @Max(value = 120, message = "La edad debe estar entre 0 y 120.")
        Integer edad,

        @NotBlank(message = "Escribe un teléfono de contacto.")
        @Size(min = 7, max = 30, message = "El teléfono debe tener entre 7 y 30 caracteres.")
        String telefono,

        @Size(max = 255, message = "La dirección es demasiado larga.")
        String direccion) {
}
