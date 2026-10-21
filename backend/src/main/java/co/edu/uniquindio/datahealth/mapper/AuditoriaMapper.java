package co.edu.uniquindio.datahealth.mapper;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.datahealth.model.dto.AuditoriaResponse;
import co.edu.uniquindio.datahealth.model.entity.RegistroAuditoria;

@Component
public class AuditoriaMapper {

    public AuditoriaResponse toResponse(RegistroAuditoria r) {
        return new AuditoriaResponse(r.getId(), r.getUsuario(), r.getAccion(), r.getDetalle(), r.getFechaHora());
    }
}