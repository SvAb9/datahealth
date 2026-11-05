package co.edu.uniquindio.datahealth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import co.edu.uniquindio.datahealth.exception.PacienteNoEncontradoException;
import co.edu.uniquindio.datahealth.mapper.CitaMapper;
import co.edu.uniquindio.datahealth.model.dto.AtencionResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistrarCitaRequest;
import co.edu.uniquindio.datahealth.model.entity.Cita;
import co.edu.uniquindio.datahealth.model.entity.Paciente;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.repository.CitaRepository;
import co.edu.uniquindio.datahealth.repository.PacienteRepository;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;

/** Pruebas unitarias de HU-05. */
@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    private static final long EPS = 7L;

    @Mock PacienteRepository pacienteRepository;
    @Mock CitaRepository citaRepository;
    @Mock AuditoriaService auditoriaService;

    CitaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new CitaService(pacienteRepository, citaRepository, auditoriaService, new CitaMapper());
        var medico = new UsuarioAutenticado(2L, "ana@eps.com", "Dra. Ana", Rol.PERSONAL_MEDICO, EPS);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(medico, null, List.of()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registraLaAtencionConLaEpsYElMedicoDelToken() {
        when(pacienteRepository.findByIdEpsAndIdPaciente(EPS, 5L)).thenReturn(Optional.of(new Paciente()));
        when(citaRepository.saveAndFlush(any(Cita.class))).thenAnswer(i -> i.getArgument(0));

        AtencionResponse r = servicio.registrar(5L, new RegistrarCitaRequest("  Hipertensión  ", "  "));

        ArgumentCaptor<Cita> guardada = ArgumentCaptor.forClass(Cita.class);
        verify(citaRepository).saveAndFlush(guardada.capture());
        assertEquals(EPS, guardada.getValue().getIdEps());
        assertEquals(5L, guardada.getValue().getIdPaciente());
        assertEquals(2L, guardada.getValue().getIdMedico());
        assertEquals("Hipertensión", guardada.getValue().getDiagnostico());
        assertEquals(null, guardada.getValue().getObservaciones());
        assertEquals("ABIERTA", r.estado());
        assertEquals("Dra. Ana", r.medico());
        verify(auditoriaService).registrar(any(), any(), any(), any());
    }

    @Test
    void pacienteDeOtraEpsSeTrataComoInexistente() {
        when(pacienteRepository.findByIdEpsAndIdPaciente(EPS, 99L)).thenReturn(Optional.empty());

        assertThrows(PacienteNoEncontradoException.class,
                () -> servicio.registrar(99L, new RegistrarCitaRequest("Dx", null)));
        verify(citaRepository, never()).saveAndFlush(any());
    }
}