package co.edu.uniquindio.datahealth.model.dto;

import co.edu.uniquindio.datahealth.model.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** No lleva id_eps: el backend lo toma del JWT del administrador (ADR-03). */
public record RegistrarUsuarioRequest(
        @NotBlank(message = "Escribe el nombre completo.")
        @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres.")
        String nombre,

        @NotBlank(message = "Escribe el correo electrónico.")
        @Email(message = "Escribe un correo válido.")
        @Size(max = 255, message = "El correo es demasiado largo.")
        String email,

        // 72 es el máximo que BCrypt procesa.
        @NotBlank(message = "Escribe la contraseña inicial.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        String password,

        @NotNull(message = "Elige un rol.")
        Rol rol) {
}
