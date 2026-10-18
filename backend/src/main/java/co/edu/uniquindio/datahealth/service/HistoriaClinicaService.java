package co.edu.uniquindio.datahealth.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.datahealth.exception.PacienteNoEncontradoException;
import co.edu.uniquindio.datahealth.mapper.CitaMapper;
import co.edu.uniquindio.datahealth.model.dto.AtencionResponse;
import co.edu.uniquindio.datahealth.model.dto.CorreccionResponse;
import co.edu.uniquindio.datahealth.model.dto.OrdenResponse;
import co.edu.uniquindio.datahealth.model.entity.Cita;
import co.edu.uniquindio.datahealth.model.entity.CorreccionCita;
import co.edu.uniquindio.datahealth.model.entity.OrdenMedica;
import co.edu.uniquindio.datahealth.model.entity.Paciente;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.repository.CitaRepository;
import co.edu.uniquindio.datahealth.repository.CorreccionCitaRepository;
import co.edu.uniquindio.datahealth.repository.OrdenRepository;
import co.edu.uniquindio.datahealth.repository.PacienteRepository;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import co.edu.uniquindio.datahealth.security.AuthContext;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;

/** HU-06 · Consultar historia clínica como línea de tiempo (RF-17, RF-19, ADR-05). */
@Service
@RequiredArgsConstructor
public class HistoriaClinicaService {

    private static final String SIN_NOMBRE = "No disponible";

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final OrdenRepository ordenRepository;
    private final CorreccionCitaRepository correccionCitaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final CitaMapper citaMapper;

    /** El paciente ve solo su propio expediente, resuelto desde su usuario en el JWT. */
    @Transactional
    public List<AtencionResponse> consultarPropia() {
        UsuarioAutenticado usuario = AuthContext.usuarioActual();
        Paciente paciente = pacienteRepository.findByIdEpsAndIdUsuario(usuario.idEps(), usuario.idUsuario())
                .orElseThrow(() -> new PacienteNoEncontradoException(
                        "No encontramos un expediente asociado a tu usuario."));
        return construir(usuario, paciente);
    }

    /** El personal médico consulta pacientes de su misma EPS; otra EPS se trata como inexistente (RN-01). */
    @Transactional
    public List<AtencionResponse> consultarDePaciente(Long idPaciente) {
        UsuarioAutenticado usuario = AuthContext.usuarioActual();
        Paciente paciente = pacienteRepository.findByIdEpsAndIdPaciente(usuario.idEps(), idPaciente)
                .orElseThrow(PacienteNoEncontradoException::new);
        return construir(usuario, paciente);
    }

    private List<AtencionResponse> construir(UsuarioAutenticado usuario, Paciente paciente) {
        Long idEps = usuario.idEps();

        List<Cita> citas = citaRepository.findByIdEpsAndIdPacienteOrderByFechaDesc(idEps, paciente.getIdPaciente());
        List<Long> idsCitas = citas.stream().map(Cita::getIdCita).toList();

        List<OrdenMedica> ordenes = idsCitas.isEmpty()
                ? List.of() : ordenRepository.findByIdEpsAndIdCitaInOrderByIdOrden(idEps, idsCitas);
        List<CorreccionCita> correcciones = idsCitas.isEmpty()
                ? List.of() : correccionCitaRepository.findByIdEpsAndIdCitaInOrderByFechaHora(idEps, idsCitas);

        // Nombres de médicos y de quienes corrigieron, en una sola consulta.
        Set<Long> idsUsuarios = new HashSet<>();
        citas.forEach(c -> idsUsuarios.add(c.getIdMedico()));
        correcciones.forEach(c -> idsUsuarios.add(c.getIdUsuario()));
        Map<Long, String> nombres = idsUsuarios.isEmpty()
                ? Map.of()
                : usuarioRepository.findByIdEpsAndIdUsuarioIn(idEps, idsUsuarios).stream()
                        .collect(Collectors.toMap(Usuario::getIdUsuario, Usuario::getNombre));

        Map<Long, List<OrdenMedica>> ordenesPorCita =
                ordenes.stream().collect(Collectors.groupingBy(OrdenMedica::getIdCita));
        Map<Long, List<CorreccionCita>> correccionesPorCita =
                correcciones.stream().collect(Collectors.groupingBy(CorreccionCita::getIdCita));

        auditoriaService.registrar(idEps, usuario.email(), "HISTORIA_CONSULTADA",
                "Paciente " + paciente.getIdPaciente());

        return citas.stream().map(cita -> {
            List<OrdenResponse> ordenesCita = ordenesPorCita.getOrDefault(cita.getIdCita(), List.of()).stream()
                    .map(citaMapper::toOrden).toList();
            List<CorreccionResponse> correccionesCita =
                    correccionesPorCita.getOrDefault(cita.getIdCita(), List.of()).stream()
                            .map(c -> citaMapper.toCorreccion(c, nombres.getOrDefault(c.getIdUsuario(), SIN_NOMBRE)))
                            .toList();
            return citaMapper.toAtencion(cita, nombres.getOrDefault(cita.getIdMedico(), SIN_NOMBRE),
                    ordenesCita, correccionesCita);
        }).toList();
    }
}
