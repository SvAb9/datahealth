package co.edu.uniquindio.datahealth.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.datahealth.mapper.AuditoriaMapper;
import co.edu.uniquindio.datahealth.model.dto.PaginaAuditoriaResponse;
import co.edu.uniquindio.datahealth.model.entity.RegistroAuditoria;
import co.edu.uniquindio.datahealth.repository.AuditoriaRepository;
import co.edu.uniquindio.datahealth.security.AuthContext;
import lombok.RequiredArgsConstructor;

/** Escribe en registro_auditoria, que es de solo inserción (ADR-06), y la consulta por EPS. */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private static final int TAMANO_MAXIMO = 100;

    private final AuditoriaRepository auditoriaRepository;
    private final AuditoriaMapper auditoriaMapper;

    public void registrar(Long idEps, String usuario, String accion, String detalle) {
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setIdEps(idEps);
        registro.setUsuario(recortar(usuario));
        registro.setAccion(accion);
        registro.setDetalle(recortar(detalle));
        registro.setFechaHora(LocalDateTime.now());
        auditoriaRepository.save(registro);
    }

    /** Registros de la EPS del administrador (id_eps del JWT), del más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public PaginaAuditoriaResponse consultar(int pagina, int tamano, String accion) {
        Long idEps = AuthContext.idEps();
        int tamanoSeguro = Math.min(Math.max(tamano, 1), TAMANO_MAXIMO);
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), tamanoSeguro,
                Sort.by(Sort.Direction.DESC, "fechaHora", "id"));
        String filtro = accion == null || accion.isBlank() ? null : accion.trim();

        Page<RegistroAuditoria> resultado = filtro == null
                ? auditoriaRepository.findByIdEps(idEps, pageable)
                : auditoriaRepository.findByIdEpsAndAccion(idEps, filtro, pageable);

        return new PaginaAuditoriaResponse(
                resultado.getContent().stream().map(auditoriaMapper::toResponse).toList(),
                resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    private String recortar(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.length() > 255 ? texto.substring(0, 255) : texto;
    }
}