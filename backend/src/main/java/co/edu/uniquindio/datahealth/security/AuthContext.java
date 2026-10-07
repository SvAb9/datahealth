package co.edu.uniquindio.datahealth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Acceso al usuario autenticado. El id_eps SIEMPRE sale de aquí, nunca de la petición (ADR-03). */
public final class AuthContext {

    private AuthContext() {
    }

    public static UsuarioAutenticado usuarioActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new IllegalStateException("No hay un usuario autenticado.");
        }
        return usuario;
    }

    public static Long idEps() {
        return usuarioActual().idEps();
    }
}