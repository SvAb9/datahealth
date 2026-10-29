package co.edu.uniquindio.datahealth.model.dto;

import co.edu.uniquindio.datahealth.util.ReglasDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Autorregistro público: no trae rol (siempre PACIENTE). El paciente elige su EPS porque aún no tiene JWT. */
public record AutorregistroPacienteRequest(
        @NotNull(message = "Elige tu entidad de salud.")
        Long idEps,

        @NotBlank(message = "Escribe el documento de identidad.")
        @Pattern(regexp = ReglasDocumento.REGEX, message = "El documento debe tener solo números, entre 5 y 15 dígitos.")
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
        String direccion,

        @NotBlank(message = "Escribe el correo electrónico.")
        @Email(message = "Escribe un correo válido.")
        @Size(max = 255, message = "El correo es demasiado largo.")
        String email,

        // 72 es el máximo que BCrypt procesa.
        @NotBlank(message = "Escribe una contraseña.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        String password) {
}