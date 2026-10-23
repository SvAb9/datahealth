package co.edu.uniquindio.datahealth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

import co.edu.uniquindio.datahealth.exception.DatosInvalidosException;
import co.edu.uniquindio.datahealth.exception.PacienteDuplicadoException;
import co.edu.uniquindio.datahealth.exception.PacienteNoEncontradoException;
import co.edu.uniquindio.datahealth.mapper.PacienteMapper;
import co.edu.uniquindio.datahealth.model.dto.PacienteResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistrarPacienteRequest;
import co.edu.uniquindio.datahealth.model.entity.Eps;
import co.edu.uniquindio.datahealth.model.entity.Paciente;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.repository.EpsRepository;
import co.edu.uniquindio.datahealth.repository.PacienteRepository;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;

/** Pruebas unitarias de HU-04 y del aislamiento por EPS (ADR-03, RN-01). No necesitan Oracle (RNF-20). */
@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    private static final long EPS_USUARIO = 7L;

    @Mock PacienteRepository pacienteRepository;
    @Mock EpsRepository epsRepository;
    @Mock AuditoriaService auditoriaService;

    PacienteService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PacienteService(pacienteRepository, epsRepository, auditoriaService, new PacienteMapper());
        var usuario = new UsuarioAutenticado(3L, "recepcion@eps.com", "Marta", Rol.PERSONAL_ADMINISTRATIVO, EPS_USUARIO);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private Eps eps() {
        Eps e = new Eps();
        e.setIdEps(EPS_USUARIO);
        e.setNombre("EPS Demo");
        return e;
    }

    @Test
    void buscaSoloEnLaEpsDelUsuarioYDevuelveElNombreDeLaEps() {
        Paciente p = new Paciente();
        p.setIdPaciente(5L);
        p.setIdEps(EPS_USUARIO);
        p.setDocumento("10203040");
        p.setNombre("Jorge Hernández");
        p.setEdad(78);
        p.setTelefono("3101234567");
        when(pacienteRepository.findByIdEpsAndDocumento(EPS_USUARIO, "10203040")).thenReturn(Optional.of(p));
        when(epsRepository.findById(EPS_USUARIO)).thenReturn(Optional.of(eps()));

        PacienteResponse r = servicio.buscarPorDocumento(" 10203040 ");

        assertEquals(5L, r.idPaciente());
        assertEquals("EPS Demo", r.eps());
    }

    @Test
    void pacienteDeOtraEpsSeTrataComoInexistente() {
        when(pacienteRepository.findByIdEpsAndDocumento(EPS_USUARIO, "99999999")).thenReturn(Optional.empty());

        assertThrows(PacienteNoEncontradoException.class, () -> servicio.buscarPorDocumento("99999999"));
    }

    @Test
    void documentoConLetrasSeRechazaSinConsultarLaBase() {
        assertThrows(DatosInvalidosException.class, () -> servicio.buscarPorDocumento("12ab"));
        verifyNoInteractions(pacienteRepository);
    }

    @Test
    void registraConLaEpsDelTokenYAudita() {
        when(pacienteRepository.existsByIdEpsAndDocumento(EPS_USUARIO, "30405060")).thenReturn(false);
        when(pacienteRepository.saveAndFlush(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));
        when(epsRepository.findById(EPS_USUARIO)).thenReturn(Optional.of(eps()));

        PacienteResponse r = servicio.registrar(
                new RegistrarPacienteRequest("30405060", "  Alberto Quintero ", 71, " 3209876543 ", "  "));

        ArgumentCaptor<Paciente> guardado = ArgumentCaptor.forClass(Paciente.class);
        verify(pacienteRepository).saveAndFlush(guardado.capture());
        assertEquals(EPS_USUARIO, guardado.getValue().getIdEps());
        assertEquals(3L, guardado.getValue().getCreadoPor());
        assertEquals("Alberto Quintero", guardado.getValue().getNombre());
        assertEquals("3209876543", guardado.getValue().getTelefono());
        assertEquals(null, guardado.getValue().getDireccion());
        assertEquals("EPS Demo", r.eps());
        verify(auditoriaService).registrar(any(), any(), any(), any());
    }

    @Test
    void rechazaDocumentoDuplicadoEnLaMismaEps() {
        when(pacienteRepository.existsByIdEpsAndDocumento(EPS_USUARIO, "10203040")).thenReturn(true);

        assertThrows(PacienteDuplicadoException.class, () -> servicio.registrar(
                new RegistrarPacienteRequest("10203040", "Jorge Hernández", 78, "3101234567", null)));
        verify(pacienteRepository, never()).saveAndFlush(any());
    }
}
