package co.edu.uniquindio.datahealth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Escribe tu correo y tu contraseña.") @Size(max = 255) String email,
        @NotBlank(message = "Escribe tu correo y tu contraseña.") @Size(max = 255) String password) {
}
