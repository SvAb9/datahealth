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
import lombok.extern.slf4j.Slf4j;

/** HU-03 · Registrar usuarios (RF-24, ADR-03, ADR-04, ADR-06). */
@Slf4j
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

        validarRol(request.rol());
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        validarEmailLibre(email);

        Usuario usuario = construirUsuario(request, email, admin);
        guardar(usuario, email);

        auditoriaService.registrar(admin.idEps(), admin.email(), "USUARIO_REGISTRADO",
                "Registró a " + email + " con rol " + request.rol());
        return usuarioMapper.toResponse(usuario);
    }

    private void validarRol(Rol rol) {
        if (!ROLES_ASIGNABLES.contains(rol)) {
            throw new RolNoPermitidoException();
        }
    }

    /** CA-02: correo ya existente (único en todo el sistema, igual que en el login). */
    private void validarEmailLibre(String email) {
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailDuplicadoException();
        }
    }

    private Usuario construirUsuario(RegistrarUsuarioRequest request, String email, UsuarioAutenticado admin) {
        Usuario usuario = new Usuario();
        usuario.setIdEps(admin.idEps());
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        usuario.setCreadoPor(admin.idUsuario());
        return usuario;
    }

    private void guardar(Usuario usuario, String email) {
        try {
            // flush para detectar aquí la violación de la restricción única si dos registros llegan a la vez.
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            log.warn("Correo duplicado detectado al guardar el usuario {}", email, e);
            throw new EmailDuplicadoException();
        }
    }
}
