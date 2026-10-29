package co.edu.uniquindio.datahealth.service;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.datahealth.model.dto.AutorregistroPacienteRequest;
import co.edu.uniquindio.datahealth.model.dto.RegistrarPacienteRequest;
import co.edu.uniquindio.datahealth.model.entity.Paciente;

/** Arma el expediente de un paciente a partir de los datos de una solicitud (sin tocar la base de datos). */
@Component
public class PacienteFactory {

    /** Registro hecho por personal de la entidad: la EPS y el autor salen del JWT. */
    public Paciente desdeRegistro(RegistrarPacienteRequest request, Long idEps, Long creadoPor) {
        return construir(idEps, null, request.documento(), request.nombre(), request.edad(),
                request.telefono(), request.direccion(), creadoPor);
    }

    /** Autorregistro: el paciente es a la vez dueño del usuario y autor del expediente. */
    public Paciente desdeAutorregistro(AutorregistroPacienteRequest request, Long idEps, Long idUsuario) {
        return construir(idEps, idUsuario, request.documento(), request.nombre(), request.edad(),
                request.telefono(), request.direccion(), idUsuario);
    }

    private Paciente construir(Long idEps, Long idUsuario, String documento, String nombre, Integer edad,
            String telefono, String direccion, Long creadoPor) {
        Paciente paciente = new Paciente();
        paciente.setIdEps(idEps);
        paciente.setIdUsuario(idUsuario);
        paciente.setDocumento(documento.trim());
        paciente.setNombre(nombre.trim());
        paciente.setEdad(edad);
        paciente.setTelefono(telefono.trim());
        paciente.setDireccion(direccion == null || direccion.isBlank() ? null : direccion.trim());
        paciente.setActivo(true);
        paciente.setCreadoPor(creadoPor);
        return paciente;
    }
}
