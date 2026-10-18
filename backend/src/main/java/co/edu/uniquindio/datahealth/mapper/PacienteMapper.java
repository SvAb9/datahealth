package co.edu.uniquindio.datahealth.mapper;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.datahealth.model.dto.PacienteResponse;
import co.edu.uniquindio.datahealth.model.entity.Paciente;

@Component
public class PacienteMapper {

    public PacienteResponse toResponse(Paciente paciente, String nombreEps) {
        return new PacienteResponse(
                paciente.getIdPaciente(),
                paciente.getDocumento(),
                paciente.getNombre(),
                paciente.getEdad(),
                paciente.getTelefono(),
                paciente.getDireccion(),
                nombreEps);
    }
}
