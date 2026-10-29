package co.edu.uniquindio.datahealth.service;

import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.datahealth.exception.DatosInvalidosException;
import co.edu.uniquindio.datahealth.exception.PacienteDuplicadoException;
import co.edu.uniquindio.datahealth.exception.PacienteNoEncontradoException;
import co.edu.uniquindio.datahealth.mapper.PacienteMapper;
import co.edu.uniquindio.datahealth.model.dto.PacienteResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistrarPacienteRequest;
import co.edu.uniquindio.datahealth.model.entity.Eps;
import co.edu.uniquindio.datahealth.model.entity.Paciente;
import co.edu.uniquindio.datahealth.repository.EpsRepository;
import co.edu.uniquindio.datahealth.repository.PacienteRepository;
import co.edu.uniquindio.datahealth.security.AuthContext;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;
import co.edu.uniquindio.datahealth.util.ReglasDocumento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Proceso 1 · HU-04: identificar o registrar al paciente (RF-06 a RF-11, ADR-03, RN-01). */
@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteService {

    private static final Pattern DOCUMENTO = Pattern.compile(ReglasDocumento.REGEX);

    private final PacienteRepository pacienteRepository;
    private final EpsRepository epsRepository;
    private final AuditoriaService auditoriaService;
    private final PacienteMapper pacienteMapper;
    private final PacienteFactory pacienteFactory;

    /** Solo busca dentro de la EPS del usuario; un paciente de otra EPS se reporta como inexistente. */
    @Transactional(readOnly = true)
    public PacienteResponse buscarPorDocumento(String documento) {
        String doc = documento == null ? "" : documento.trim();
        if (!DOCUMENTO.matcher(doc).matches()) {
            throw new DatosInvalidosException("El documento debe tener solo números, entre 5 y 15 dígitos.");
        }
        Long idEps = AuthContext.idEps();
        Paciente paciente = pacienteRepository.findByIdEpsAndDocumento(idEps, doc)
                .orElseThrow(PacienteNoEncontradoException::new);
        return pacienteMapper.toResponse(paciente, nombreEps(idEps));
    }

    @Transactional
    public PacienteResponse registrar(RegistrarPacienteRequest request) {
        // El id_eps sale del JWT, nunca de la petición (ADR-03, RF-10).
        UsuarioAutenticado usuario = AuthContext.usuarioActual();
        String doc = request.documento().trim();

        // RF-08: no se permiten duplicados dentro de la misma EPS.
        if (pacienteRepository.existsByIdEpsAndDocumento(usuario.idEps(), doc)) {
            throw new PacienteDuplicadoException();
        }

        Paciente paciente = pacienteFactory.desdeRegistro(request, usuario.idEps(), usuario.idUsuario());

        try {
            // flush para detectar aquí la restricción única si dos registros llegan a la vez.
            pacienteRepository.saveAndFlush(paciente);
        } catch (DataIntegrityViolationException e) {
            log.warn("Violación de integridad al registrar el paciente: {}", e.getMostSpecificCause().getMessage());
            throw new PacienteDuplicadoException();
        }

        auditoriaService.registrar(usuario.idEps(), usuario.email(), "PACIENTE_REGISTRADO",
                "Registró al paciente " + paciente.getIdPaciente());
        return pacienteMapper.toResponse(paciente, nombreEps(usuario.idEps()));
    }

    private String nombreEps(Long idEps) {
        return epsRepository.findById(idEps).map(Eps::getNombre).orElse(null);
    }
}