package co.edu.uniquindio.datahealth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.datahealth.exception.PacienteNoEncontradoException;
import co.edu.uniquindio.datahealth.mapper.CitaMapper;
import co.edu.uniquindio.datahealth.model.dto.AtencionResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistrarCitaRequest;
import co.edu.uniquindio.datahealth.model.entity.Cita;
import co.edu.uniquindio.datahealth.model.enums.EstadoCita;
import co.edu.uniquindio.datahealth.repository.CitaRepository;
import co.edu.uniquindio.datahealth.repository.PacienteRepository;
import co.edu.uniquindio.datahealth.security.AuthContext;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;

/** HU-05 · Registrar la información clínica de una atención (RF-16, ADR-03, ADR-05). */
@Service
@RequiredArgsConstructor
public class CitaService {

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final AuditoriaService auditoriaService;
    private final CitaMapper citaMapper;

    @Transactional
    public AtencionResponse registrar(Long idPaciente, RegistrarCitaRequest request) {
        UsuarioAutenticado medico = AuthContext.usuarioActual();

        // Un paciente de otra EPS se trata como inexistente (RN-01).
        pacienteRepository.findByIdEpsAndIdPaciente(medico.idEps(), idPaciente)
                .orElseThrow(PacienteNoEncontradoException::new);

        Cita cita = new Cita();
        cita.setIdEps(medico.idEps());
        cita.setIdPaciente(idPaciente);
        cita.setIdMedico(medico.idUsuario());
        cita.setFecha(LocalDateTime.now());
        cita.setEstado(EstadoCita.ABIERTA);
        cita.setDiagnostico(request.diagnostico().trim());
        cita.setObservaciones(vacioANulo(request.observaciones()));
        cita.setCreadoPor(medico.idUsuario());
        citaRepository.saveAndFlush(cita);

        auditoriaService.registrar(medico.idEps(), medico.email(), "CITA_REGISTRADA",
                "Atención " + cita.getIdCita() + " del paciente " + idPaciente);

        return citaMapper.toAtencion(cita, medico.nombre(), List.of(), List.of());
    }

    private String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}