package co.edu.uniquindio.datahealth.service;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.datahealth.exception.EmailDuplicadoException;
import co.edu.uniquindio.datahealth.exception.RolNoPermitidoException;
import co.edu.uniquindio.datahealth.mapper.UsuarioMapper;
import co.edu.uniquindio.datahealth.model.dto.RegistrarUsuarioRequest;
import co.edu.uniquindio.datahealth.model.dto.UsuarioResponse;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import co.edu.uniquindio.datahealth.security.AuthContext;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;

/** HU-03 · Registrar usuarios (RF-24, ADR-03, ADR-04, ADR-06). */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    /** Roles que un administrador de entidad puede crear. Otro administrador se crea por script. */
    private static final Set<Rol> ROLES_ASIGNABLES = EnumSet.of(Rol.PERSONAL_MEDICO, Rol.PACIENTE, Rol.PERSONAL_ADMINISTRATIVO);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final UsuarioMapper usuarioMapper;

    @Transactional
    public UsuarioResponse registrar(RegistrarUsuarioRequest request) {
        // El id_eps sale del JWT del administrador, nunca de la petición (ADR-03, RN-01).
        UsuarioAutenticado admin = AuthContext.usuarioActual();

        if (!ROLES_ASIGNABLES.contains(request.rol())) {
            throw new RolNoPermitidoException();
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        // CA-02: correo ya existente (único en todo el sistema, igual que en el login).
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailDuplicadoException();
        }

        Usuario usuario = new Usuario();
        usuario.setIdEps(admin.idEps());
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        usuario.setCreadoPor(admin.idUsuario());

        try {
            // flush para detectar aquí la violación de la restricción única si dos registros llegan a la vez.
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new EmailDuplicadoException();
        }

        auditoriaService.registrar(admin.idEps(), admin.email(), "USUARIO_REGISTRADO",
                "Registró a " + email + " con rol " + request.rol());
        return usuarioMapper.toResponse(usuario);
    }
}
