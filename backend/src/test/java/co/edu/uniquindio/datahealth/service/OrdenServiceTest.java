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

import co.edu.uniquindio.datahealth.exception.ConflictoException;
import co.edu.uniquindio.datahealth.exception.DatosInvalidosException;
import co.edu.uniquindio.datahealth.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.datahealth.mapper.CitaMapper;
import co.edu.uniquindio.datahealth.model.dto.OrdenResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistrarOrdenRequest;
import co.edu.uniquindio.datahealth.model.dto.RegistrarResultadoRequest;
import co.edu.uniquindio.datahealth.model.entity.Cita;
import co.edu.uniquindio.datahealth.model.entity.OrdenMedica;
import co.edu.uniquindio.datahealth.model.entity.ResultadoExamen;
import co.edu.uniquindio.datahealth.model.enums.EstadoCita;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.model.enums.TipoOrden;
import co.edu.uniquindio.datahealth.repository.CitaRepository;
import co.edu.uniquindio.datahealth.repository.OrdenRepository;
import co.edu.uniquindio.datahealth.repository.ResultadoExamenRepository;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;

/** Pruebas unitarias de HU-07: órdenes, resultados, cita cerrada y aislamiento por EPS. */
@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    private static final long EPS = 7L;

    @Mock CitaRepository citaRepository;
    @Mock OrdenRepository ordenRepository;
    @Mock ResultadoExamenRepository resultadoRepository;
    @Mock AuditoriaService auditoriaService;

    OrdenService servicio;

    @BeforeEach
    void preparar() {
        servicio = new OrdenService(citaRepository, ordenRepository, resultadoRepository,
                auditoriaService, new CitaMapper());
        var medico = new UsuarioAutenticado(2L, "ana@eps.com", "Dra. Ana", Rol.PERSONAL_MEDICO, EPS);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(medico, null, List.of()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private Cita cita(EstadoCita estado) {
        Cita c = new Cita();
        c.setIdCita(11L);
        c.setEstado(estado);
        return c;
    }

    private OrdenMedica orden(TipoOrden tipo) {
        OrdenMedica o = new OrdenMedica();
        o.setIdOrden(3L);
        o.setIdCita(11L);
        o.setTipo(tipo);
        return o;
    }

    @Test
    void agregaLaOrdenVinculadaALaCitaConLaEpsYElMedicoDelToken() {
        when(citaRepository.findByIdEpsAndIdCita(EPS, 11L)).thenReturn(Optional.of(cita(EstadoCita.ABIERTA)));
        when(ordenRepository.saveAndFlush(any(OrdenMedica.class))).thenAnswer(i -> i.getArgument(0));

        OrdenResponse r = servicio.agregarOrden(11L,
                new RegistrarOrdenRequest(TipoOrden.MEDICAMENTO, "  Losartán 50 mg  "));

        ArgumentCaptor<OrdenMedica> guardada = ArgumentCaptor.forClass(OrdenMedica.class);
        verify(ordenRepository).saveAndFlush(guardada.capture());
        assertEquals(EPS, guardada.getValue().getIdEps());
        assertEquals(11L, guardada.getValue().getIdCita());
        assertEquals(2L, guardada.getValue().getCreadoPor());
        assertEquals("Losartán 50 mg", guardada.getValue().getDescripcion());
        assertEquals("MEDICAMENTO", r.tipo());
        verify(auditoriaService).registrar(any(), any(), any(), any());
    }

    @Test
    void citaCerradaRechazaNuevasOrdenes() {
        when(citaRepository.findByIdEpsAndIdCita(EPS, 11L)).thenReturn(Optional.of(cita(EstadoCita.CERRADA)));

        assertThrows(ConflictoException.class,
                () -> servicio.agregarOrden(11L, new RegistrarOrdenRequest(TipoOrden.MEDICA, "Control")));
        verify(ordenRepository, never()).saveAndFlush(any());
    }

    @Test
    void citaDeOtraEpsSeTrataComoInexistente() {
        when(citaRepository.findByIdEpsAndIdCita(EPS, 99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.agregarOrden(99L, new RegistrarOrdenRequest(TipoOrden.MEDICA, "Control")));
        verify(ordenRepository, never()).saveAndFlush(any());
    }

    @Test
    void registraElResultadoDeUnaOrdenDeLaboratorio() {
        when(ordenRepository.findByIdEpsAndIdOrden(EPS, 3L)).thenReturn(Optional.of(orden(TipoOrden.LABORATORIO)));
        when(resultadoRepository.existsByIdEpsAndIdOrden(EPS, 3L)).thenReturn(false);
        when(resultadoRepository.saveAndFlush(any(ResultadoExamen.class))).thenAnswer(i -> i.getArgument(0));

        servicio.registrarResultado(3L, new RegistrarResultadoRequest("  Glucosa 98 mg/dL  "));

        ArgumentCaptor<ResultadoExamen> guardado = ArgumentCaptor.forClass(ResultadoExamen.class);
        verify(resultadoRepository).saveAndFlush(guardado.capture());
        assertEquals(EPS, guardado.getValue().getIdEps());
        assertEquals(3L, guardado.getValue().getIdOrden());
        assertEquals(11L, guardado.getValue().getIdCita());
        assertEquals("Glucosa 98 mg/dL", guardado.getValue().getResultado());
    }

    @Test
    void soloLasOrdenesDeLaboratorioAdmitenResultado() {
        when(ordenRepository.findByIdEpsAndIdOrden(EPS, 3L)).thenReturn(Optional.of(orden(TipoOrden.MEDICAMENTO)));

        assertThrows(DatosInvalidosException.class,
                () -> servicio.registrarResultado(3L, new RegistrarResultadoRequest("Normal")));
        verify(resultadoRepository, never()).saveAndFlush(any());
    }

    @Test
    void rechazaUnSegundoResultadoParaLaMismaOrden() {
        when(ordenRepository.findByIdEpsAndIdOrden(EPS, 3L)).thenReturn(Optional.of(orden(TipoOrden.LABORATORIO)));
        when(resultadoRepository.existsByIdEpsAndIdOrden(EPS, 3L)).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> servicio.registrarResultado(3L, new RegistrarResultadoRequest("Otro")));
        verify(resultadoRepository, never()).saveAndFlush(any());
    }

    @Test
    void ordenDeOtraEpsSeTrataComoInexistente() {
        when(ordenRepository.findByIdEpsAndIdOrden(EPS, 99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.registrarResultado(99L, new RegistrarResultadoRequest("Normal")));
    }
}