package co.edu.uniquindio.datahealth.model.dto;

import co.edu.uniquindio.datahealth.model.enums.Rol;

/** Nunca incluye el hash de la contraseña. */
public record UsuarioResponse(Long idUsuario, String nombre, String email, Rol rol, boolean activo) {
}
