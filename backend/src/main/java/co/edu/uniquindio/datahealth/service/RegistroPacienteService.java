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
import co.edu.uniquindio.datahealth.model.dto.RegistroPacienteRequest;
import co.edu.uniquindio.datahealth.model.entity.Eps;
import co.edu.uniquindio.datahealth.model.entity.Paciente;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.repository.EpsRepository;
import co.edu.uniquindio.datahealth.repository.PacienteRepository;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import co.edu.uniquindio.datahealth.security.JwtService;
import lombok.RequiredArgsConstructor;

/** Autorregistro público: SOLO crea usuarios con rol PACIENTE (los demás roles los crea el administrador). */
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

    @Transactional(readOnly = true)
    public List<EpsResponse> listarEntidades() {
        return epsRepository.findByActivaTrueOrderByNombreAsc().stream()
                .map(e -> new EpsResponse(e.getIdEps(), e.getNombre()))
                .toList();
    }

    @Transactional
    public LoginResponse registrar(RegistroPacienteRequest request) {
        Eps eps = epsRepository.findById(request.idEps())
                .filter(e -> Boolean.TRUE.equals(e.getActiva()))
                .orElseThrow(() -> new DatosInvalidosException("Elige una entidad de salud válida."));

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailDuplicadoException();
        }

        // Si el documento ya tiene expediente NO se vincula solo: así nadie puede reclamar
        // la historia clínica de otra persona conociendo apenas su documento.
        String documento = request.documento().trim();
        if (pacienteRepository.existsByIdEpsAndDocumento(eps.getIdEps(), documento)) {
            throw new PacienteDuplicadoException(MENSAJE_DOCUMENTO);
        }

        Usuario usuario = new Usuario();
        usuario.setIdEps(eps.getIdEps());
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.PACIENTE);
        usuario.setActivo(true);
        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new EmailDuplicadoException();
        }

        Paciente paciente = new Paciente();
        paciente.setIdEps(eps.getIdEps());
        paciente.setIdUsuario(usuario.getIdUsuario());
        paciente.setDocumento(documento);
        paciente.setNombre(request.nombre().trim());
        paciente.setEdad(request.edad());
        paciente.setTelefono(request.telefono().trim());
        paciente.setDireccion(request.direccion() == null || request.direccion().isBlank()
                ? null : request.direccion().trim());
        paciente.setActivo(true);
        paciente.setCreadoPor(usuario.getIdUsuario());
        try {
            pacienteRepository.saveAndFlush(paciente);
        } catch (DataIntegrityViolationException e) {
            throw new PacienteDuplicadoException(MENSAJE_DOCUMENTO);
        }

        auditoriaService.registrar(eps.getIdEps(), email, "PACIENTE_AUTOREGISTRADO",
                "Se registró desde el inicio de sesión (paciente " + paciente.getIdPaciente() + ")");
        return new LoginResponse(jwtService.generarToken(usuario));
    }
}