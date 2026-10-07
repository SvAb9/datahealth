package co.edu.uniquindio.datahealth.security;

import co.edu.uniquindio.datahealth.model.enums.Rol;

/** Datos del usuario que viajan en el JWT y quedan en el contexto de seguridad. */
public record UsuarioAutenticado(Long idUsuario, String email, String nombre, Rol rol, Long idEps) {
}