package co.edu.uniquindio.datahealth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.datahealth.exception.ConflictoException;
import co.edu.uniquindio.datahealth.exception.DatosInvalidosException;
import co.edu.uniquindio.datahealth.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.datahealth.mapper.CitaMapper;
import co.edu.uniquindio.datahealth.model.dto.OrdenResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistrarOrdenRequest;
import co.edu.uniquindio.datahealth.model.dto.RegistrarResultadoRequest;
import co.edu.uniquindio.datahealth.model.dto.ResultadoResponse;
import co.edu.uniquindio.datahealth.model.entity.Cita;
import co.edu.uniquindio.datahealth.model.entity.OrdenMedica;
import co.edu.uniquindio.datahealth.model.entity.ResultadoExamen;
import co.edu.uniquindio.datahealth.model.enums.EstadoCita;
import co.edu.uniquindio.datahealth.model.enums.TipoOrden;
import co.edu.uniquindio.datahealth.repository.CitaRepository;
import co.edu.uniquindio.datahealth.repository.OrdenRepository;
import co.edu.uniquindio.datahealth.repository.ResultadoExamenRepository;
import co.edu.uniquindio.datahealth.security.AuthContext;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;

/** HU-07 · Órdenes y resultados de una atención (RF-13 a RF-16, ADR-03, ADR-05). */
@Service
@RequiredArgsConstructor
public class OrdenService {

    private final CitaRepository citaRepository;
    private final OrdenRepository ordenRepository;
    private final ResultadoExamenRepository resultadoRepository;
    private final AuditoriaService auditoriaService;
    private final CitaMapper citaMapper;

    /** CA-01: la orden queda vinculada a la cita (y por ella, al paciente). Solo con la cita ABIERTA. */
    @Transactional
    public OrdenResponse agregarOrden(Long idCita, RegistrarOrdenRequest request) {
        UsuarioAutenticado medico = AuthContext.usuarioActual();

        Cita cita = citaRepository.findByIdEpsAndIdCita(medico.idEps(), idCita)
                .orElseThrow(() -> new RecursoNoEncontradoException("No encontramos la atención indicada."));
        if (cita.getEstado() == EstadoCita.CERRADA) {
            throw new ConflictoException(
                    "La atención está cerrada: no se pueden agregar órdenes. Registra una nueva atención.");
        }

        OrdenMedica orden = new OrdenMedica();
        orden.setIdEps(medico.idEps());
        orden.setIdCita(cita.getIdCita());
        orden.setTipo(request.tipo());
        orden.setDescripcion(request.descripcion().trim());
        orden.setCreadoPor(medico.idUsuario());
        ordenRepository.saveAndFlush(orden);

        auditoriaService.registrar(medico.idEps(), medico.email(), "ORDEN_REGISTRADA",
                "Orden " + orden.getIdOrden() + " (" + orden.getTipo() + ") en la atención " + idCita);
        return citaMapper.toOrden(orden);
    }

    /** CA-02: el resultado puede llegar después del cierre de la cita (ADR-05). */
    @Transactional
    public ResultadoResponse registrarResultado(Long idOrden, RegistrarResultadoRequest request) {
        UsuarioAutenticado medico = AuthContext.usuarioActual();

        OrdenMedica orden = ordenRepository.findByIdEpsAndIdOrden(medico.idEps(), idOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException("No encontramos la orden indicada."));
        if (orden.getTipo() != TipoOrden.LABORATORIO) {
            throw new DatosInvalidosException("Solo las órdenes de laboratorio tienen resultado.");
        }
        if (resultadoRepository.existsByIdEpsAndIdOrden(medico.idEps(), idOrden)) {
            throw new ConflictoException("Esta orden ya tiene un resultado registrado.");
        }

        ResultadoExamen resultado = new ResultadoExamen();
        resultado.setIdEps(medico.idEps());
        resultado.setIdOrden(orden.getIdOrden());
        resultado.setIdCita(orden.getIdCita());
        resultado.setResultado(request.resultado().trim());
        resultado.setFechaResultado(LocalDateTime.now());
        resultado.setCreadoPor(medico.idUsuario());
        resultadoRepository.saveAndFlush(resultado);

        auditoriaService.registrar(medico.idEps(), medico.email(), "RESULTADO_REGISTRADO",
                "Resultado de la orden " + idOrden);
        return citaMapper.toResultado(resultado);
    }
}