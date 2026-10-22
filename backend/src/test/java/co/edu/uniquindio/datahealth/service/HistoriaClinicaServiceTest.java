package co.edu.uniquindio.datahealth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import co.edu.uniquindio.datahealth.exception.PacienteNoEncontradoException;
import co.edu.uniquindio.datahealth.mapper.CitaMapper;
import co.edu.uniquindio.datahealth.model.dto.AtencionResponse;
import co.edu.uniquindio.datahealth.model.entity.Cita;
import co.edu.uniquindio.datahealth.model.entity.CorreccionCita;
import co.edu.uniquindio.datahealth.model.entity.OrdenMedica;
import co.edu.uniquindio.datahealth.model.entity.Paciente;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.EstadoCita;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.model.enums.TipoOrden;
import co.edu.uniquindio.datahealth.repository.CitaRepository;
import co.edu.uniquindio.datahealth.repository.CorreccionCitaRepository;
import co.edu.uniquindio.datahealth.repository.OrdenRepository;
import co.edu.uniquindio.datahealth.repository.PacienteRepository;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;

/** Pruebas unitarias de HU-06: línea de tiempo y aislamiento por EPS. */
@ExtendWith(MockitoExtension.class)
class HistoriaClinicaServiceTest {

    private static final long EPS = 7L;

    @Mock PacienteRepository pacienteRepository;
    @Mock CitaRepository citaRepository;
    @Mock OrdenRepository ordenRepository;
    @Mock CorreccionCitaRepository correccionCitaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock AuditoriaService auditoriaService;

    HistoriaClinicaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new HistoriaClinicaService(pacienteRepository, citaRepository, ordenRepository,
                correccionCitaRepository, usuarioRepository, auditoriaService, new CitaMapper());
        var medico = new UsuarioAutenticado(2L, "ana@eps.com", "Dra. Ana", Rol.PERSONAL_MEDICO, EPS);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(medico, null, List.of()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pacienteDeOtraEpsSeTrataComoInexistenteYNoSeLeenCitas() {
        when(pacienteRepository.findByIdEpsAndIdPaciente(EPS, 99L)).thenReturn(Optional.empty());

        assertThrows(PacienteNoEncontradoException.class, () -> servicio.consultarDePaciente(99L));
        verifyNoInteractions(citaRepository, ordenRepository, correccionCitaRepository);
    }

    @Test
    void usuarioPacienteSinExpedienteRecibeNoEncontrado() {
        var paciente = new UsuarioAutenticado(9L, "p@eps.com", "Paciente", Rol.PACIENTE, EPS);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(paciente, null, List.of()));
        when(pacienteRepository.findByIdEpsAndIdUsuario(EPS, 9L)).thenReturn(Optional.empty());

        assertThrows(PacienteNoEncontradoException.class, () -> servicio.consultarPropia());
    }

    @Test
    void armaLaLineaDeTiempoConOrdenesCorreccionesYNombres() {
        Paciente p = new Paciente();
        p.setIdPaciente(5L);
        when(pacienteRepository.findByIdEpsAndIdPaciente(EPS, 5L)).thenReturn(Optional.of(p));

        Cita c = new Cita();
        c.setIdCita(11L);
        c.setIdMedico(2L);
        c.setFecha(LocalDateTime.of(2026, 9, 1, 10, 0));
        c.setEstado(EstadoCita.CERRADA);
        c.setDiagnostico("Hipertensión arterial");
        when(citaRepository.findByIdEpsAndIdPacienteOrderByFechaDesc(EPS, 5L)).thenReturn(List.of(c));

        OrdenMedica o = new OrdenMedica();
        o.setIdCita(11L);
        o.setTipo(TipoOrden.MEDICAMENTO);
        o.setDescripcion("Losartán 50 mg");
        when(ordenRepository.findByIdEpsAndIdCitaInOrderByIdOrden(EPS, List.of(11L))).thenReturn(List.of(o));

        CorreccionCita cc = new CorreccionCita();
        cc.setIdCita(11L);
        cc.setIdUsuario(2L);
        cc.setMotivo("Corrección de dosis");
        cc.setFechaHora(LocalDateTime.of(2026, 9, 3, 9, 0));
        when(correccionCitaRepository.findByIdEpsAndIdCitaInOrderByFechaHora(EPS, List.of(11L)))
                .thenReturn(List.of(cc));

        Usuario medico = new Usuario();
        medico.setIdUsuario(2L);
        medico.setNombre("Dra. Ana");
        when(usuarioRepository.findByIdEpsAndIdUsuarioIn(eq(EPS), any())).thenReturn(List.of(medico));

        List<AtencionResponse> r = servicio.consultarDePaciente(5L);

        assertEquals(1, r.size());
        assertEquals("CERRADA", r.get(0).estado());
        assertEquals("Dra. Ana", r.get(0).medico());
        assertEquals("MEDICAMENTO", r.get(0).ordenes().get(0).tipo());
        assertEquals("Dra. Ana", r.get(0).correcciones().get(0).usuario());
        verify(auditoriaService).registrar(eq(EPS), eq("ana@eps.com"), eq("HISTORIA_CONSULTADA"), anyString());
    }

    @Test
    void pacienteSinAtencionesDevuelveListaVacia() {
        Paciente p = new Paciente();
        p.setIdPaciente(6L);
        when(pacienteRepository.findByIdEpsAndIdPaciente(EPS, 6L)).thenReturn(Optional.of(p));
        when(citaRepository.findByIdEpsAndIdPacienteOrderByFechaDesc(EPS, 6L)).thenReturn(List.of());

        assertEquals(0, servicio.consultarDePaciente(6L).size());
        verifyNoInteractions(ordenRepository, correccionCitaRepository, usuarioRepository);
    }
}
