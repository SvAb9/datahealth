package co.edu.uniquindio.datahealth.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.datahealth.model.dto.AtencionResponse;
import co.edu.uniquindio.datahealth.model.dto.CorreccionResponse;
import co.edu.uniquindio.datahealth.model.dto.OrdenResponse;
import co.edu.uniquindio.datahealth.model.entity.Cita;
import co.edu.uniquindio.datahealth.model.entity.CorreccionCita;
import co.edu.uniquindio.datahealth.model.entity.OrdenMedica;

@Component
public class CitaMapper {

    public AtencionResponse toAtencion(Cita cita, String medico, List<OrdenResponse> ordenes,
                                       List<CorreccionResponse> correcciones) {
        return new AtencionResponse(
                cita.getIdCita(),
                cita.getFecha(),
                cita.getEstado().name(),
                medico,
                cita.getDiagnostico(),
                ordenes,
                correcciones);
    }

    public OrdenResponse toOrden(OrdenMedica orden) {
        return new OrdenResponse(orden.getTipo().name(), orden.getDescripcion());
    }

    public CorreccionResponse toCorreccion(CorreccionCita correccion, String usuario) {
        return new CorreccionResponse(correccion.getMotivo(), usuario, correccion.getFechaHora());
    }
}
