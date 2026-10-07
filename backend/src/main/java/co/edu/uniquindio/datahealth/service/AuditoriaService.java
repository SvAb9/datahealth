package co.edu.uniquindio.datahealth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import co.edu.uniquindio.datahealth.model.entity.RegistroAuditoria;
import co.edu.uniquindio.datahealth.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;

/** Escribe en registro_auditoria, que es de solo inserción (ADR-06). */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public void registrar(Long idEps, String usuario, String accion, String detalle) {
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setIdEps(idEps);
        registro.setUsuario(recortar(usuario));
        registro.setAccion(accion);
        registro.setDetalle(recortar(detalle));
        registro.setFechaHora(LocalDateTime.now());
        auditoriaRepository.save(registro);
    }

    private String recortar(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.length() > 255 ? texto.substring(0, 255) : texto;
    }
}