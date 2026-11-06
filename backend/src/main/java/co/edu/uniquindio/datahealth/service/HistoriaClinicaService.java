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
import co.edu.uniquindio.datahealth.repository.ResultadoExamenRepository;
import co.edu.uniquindio.datahealth.model.entity.ResultadoExamen;
import co.edu.uniquindio.datahealth.model.dto.ResultadoResponse;

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
    private final ResultadoExamenRepository resultadoRepository;
    /** El paciente ve solo su propio expediente, resuelto desde su usuario en el JWT. */
    @Transactional
    public List<AtencionResponse> consultarPropia() {
        UsuarioAutenticado usuario = AuthContext.usuarioActual();
        Paciente paciente = pacienteRepository.findByIdEpsAndIdUsuario(usuario.idEps(), usuario.idUsuario())
                .orElseThrow(() -> new PacienteNoEncontradoException(
                        "No encontramos un expediente asociado a tu usuario."));
        return armarHistoria(usuario, paciente);
    }

    /** El personal médico consulta pacientes de su misma EPS; otra EPS se trata como inexistente (RN-01). */
    @Transactional
    public List<AtencionResponse> consultarDePaciente(Long idPaciente) {
        UsuarioAutenticado usuario = AuthContext.usuarioActual();
        Paciente paciente = pacienteRepository.findByIdEpsAndIdPaciente(usuario.idEps(), idPaciente)
                .orElseThrow(PacienteNoEncontradoException::new);
        return armarHistoria(usuario, paciente);
    }

    private List<AtencionResponse> armarHistoria(UsuarioAutenticado usuario, Paciente paciente) {
        Long idEps = usuario.idEps();

        List<Cita> citas = citaRepository.findByIdEpsAndIdPacienteOrderByFechaDesc(idEps, paciente.getIdPaciente());
        List<Long> idsCitas = citas.stream().map(Cita::getIdCita).toList();

        List<OrdenMedica> ordenes = idsCitas.isEmpty()
                ? List.of() : ordenRepository.findByIdEpsAndIdCitaInOrderByIdOrden(idEps, idsCitas);
        List<CorreccionCita> correcciones = idsCitas.isEmpty()
                ? List.of() : correccionCitaRepository.findByIdEpsAndIdCitaInOrderByFechaHora(idEps, idsCitas);
        List<ResultadoExamen> resultados = idsCitas.isEmpty()
                ? List.of() : resultadoRepository.findByIdEpsAndIdCitaInOrderByFechaResultado(idEps, idsCitas);
        
        Map<Long, String> nombres = nombresDeUsuarios(idEps, citas, correcciones);

        auditoriaService.registrar(idEps, usuario.email(), "HISTORIA_CONSULTADA",
                "Paciente " + paciente.getIdPaciente());

         return armarAtenciones(citas, ordenes, correcciones, resultados, nombres);
    }

    /** Nombres de médicos y de quienes corrigieron, en una sola consulta. */
    private Map<Long, String> nombresDeUsuarios(Long idEps, List<Cita> citas, List<CorreccionCita> correcciones) {
        Set<Long> idsUsuarios = new HashSet<>();
        citas.forEach(c -> idsUsuarios.add(c.getIdMedico()));
        correcciones.forEach(c -> idsUsuarios.add(c.getIdUsuario()));
        if (idsUsuarios.isEmpty()) {
            return Map.of();
        }
        return usuarioRepository.findByIdEpsAndIdUsuarioIn(idEps, idsUsuarios).stream()
                .collect(Collectors.toMap(Usuario::getIdUsuario, Usuario::getNombre));
    }

    private List<AtencionResponse> armarAtenciones(List<Cita> citas, List<OrdenMedica> ordenes,
            List<CorreccionCita> correcciones, List<ResultadoExamen> resultados, Map<Long, String> nombres) {
        Map<Long, List<OrdenMedica>> ordenesPorCita =
                ordenes.stream().collect(Collectors.groupingBy(OrdenMedica::getIdCita));
        Map<Long, List<CorreccionCita>> correccionesPorCita =
                correcciones.stream().collect(Collectors.groupingBy(CorreccionCita::getIdCita));
        Map<Long, List<ResultadoExamen>> resultadosPorCita =
                resultados.stream().collect(Collectors.groupingBy(ResultadoExamen::getIdCita));

        return citas.stream().map(cita -> {
            List<OrdenResponse> ordenesCita = ordenesPorCita.getOrDefault(cita.getIdCita(), List.of()).stream()
                    .map(citaMapper::toOrden).toList();
            List<CorreccionResponse> correccionesCita =
                    correccionesPorCita.getOrDefault(cita.getIdCita(), List.of()).stream()
                            .map(c -> citaMapper.toCorreccion(c, nombres.getOrDefault(c.getIdUsuario(), SIN_NOMBRE)))
                            .toList();
            List<ResultadoResponse> resultadosCita =
                    resultadosPorCita.getOrDefault(cita.getIdCita(), List.of()).stream()
                            .map(citaMapper::toResultado).toList();
            return citaMapper.toAtencion(cita, nombres.getOrDefault(cita.getIdMedico(), SIN_NOMBRE),
                    ordenesCita, correccionesCita, resultadosCita);
        }).toList();
    }
}
