package co.edu.uniquindio.datahealth.service;

import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.datahealth.exception.DatosInvalidosException;
import co.edu.uniquindio.datahealth.exception.EmailDuplicadoException;
import co.edu.uniquindio.datahealth.exception.PacienteDuplicadoException;
import co.edu.uniquindio.datahealth.model.dto.EpsResponse;
import co.edu.uniquindio.datahealth.model.dto.LoginResponse;
import co.edu.uniquindio.datahealth.model.dto.AutorregistroPacienteRequest;
import co.edu.uniquindio.datahealth.model.entity.Eps;
import co.edu.uniquindio.datahealth.model.entity.Paciente;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.repository.EpsRepository;
import co.edu.uniquindio.datahealth.repository.PacienteRepository;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import co.edu.uniquindio.datahealth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Autorregistro público: SOLO crea usuarios con rol PACIENTE (los demás roles los crea el administrador). */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistroPacienteService {

    private static final String MENSAJE_DOCUMENTO = "No pudimos crear tu cuenta con esos datos. "
            + "Si ya te atendieron en tu entidad de salud, pide al administrador que cree tu usuario.";

    private final EpsRepository epsRepository;
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaService auditoriaService;
    private final PacienteFactory pacienteFactory;


    @Transactional(readOnly = true)
    public List<EpsResponse> listarEntidades() {
        return epsRepository.findByActivaTrueOrderByNombreAsc().stream()
                .map(e -> new EpsResponse(e.getIdEps(), e.getNombre()))
                .toList();
    }

    @Transactional
    public LoginResponse registrar(AutorregistroPacienteRequest request) {
        Eps eps = buscarEpsActiva(request.idEps());
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        validarEmailLibre(email);
        // Si el documento ya tiene expediente NO se vincula solo: así nadie puede reclamar
        // la historia clínica de otra persona conociendo apenas su documento.
        validarDocumentoLibre(eps.getIdEps(), request.documento().trim());

        Usuario usuario = crearUsuario(request, eps.getIdEps(), email);
        Paciente paciente = crearPaciente(request, eps.getIdEps(), usuario);

        auditoriaService.registrar(eps.getIdEps(), email, "PACIENTE_AUTOREGISTRADO",
                "Se registró desde el inicio de sesión (paciente " + paciente.getIdPaciente() + ")");
        return new LoginResponse(jwtService.generarToken(usuario));
    }

    private Eps buscarEpsActiva(Long idEps) {
        return epsRepository.findById(idEps)
                .filter(e -> Boolean.TRUE.equals(e.getActiva()))
                .orElseThrow(() -> new DatosInvalidosException("Elige una entidad de salud válida."));
    }

    private void validarEmailLibre(String email) {
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailDuplicadoException();
        }
    }

    private void validarDocumentoLibre(Long idEps, String documento) {
        if (pacienteRepository.existsByIdEpsAndDocumento(idEps, documento)) {
            throw new PacienteDuplicadoException(MENSAJE_DOCUMENTO);
        }
    }

    private Usuario crearUsuario(AutorregistroPacienteRequest request, Long idEps, String email) {
        Usuario usuario = new Usuario();
        usuario.setIdEps(idEps);
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.PACIENTE);
        usuario.setActivo(true);
        try {
            usuarioRepository.saveAndFlush(usuario);
            return usuario;
        } catch (DataIntegrityViolationException e) {
            log.warn("Violación de integridad al autorregistrar el usuario: {}", e.getMostSpecificCause().getMessage());
            throw new EmailDuplicadoException();
        }
    }

    private Paciente crearPaciente(AutorregistroPacienteRequest request, Long idEps, Usuario usuario) {
        Paciente paciente = pacienteFactory.desdeAutorregistro(request, idEps, usuario.getIdUsuario());
        try {
            pacienteRepository.saveAndFlush(paciente);
            return paciente;
        } catch (DataIntegrityViolationException e) {
            log.warn("Violación de integridad al registrar el paciente: {}", e.getMostSpecificCause().getMessage());
            throw new PacienteDuplicadoException(MENSAJE_DOCUMENTO);
        }
    }
}
