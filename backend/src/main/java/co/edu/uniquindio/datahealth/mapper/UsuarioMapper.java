package co.edu.uniquindio.datahealth.mapper;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.datahealth.model.dto.UsuarioResponse;
import co.edu.uniquindio.datahealth.model.entity.Usuario;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                Boolean.TRUE.equals(usuario.getActivo()));
    }
}
