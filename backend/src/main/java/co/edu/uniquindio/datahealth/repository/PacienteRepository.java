package co.edu.uniquindio.datahealth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.Paciente;

/** ADR-03: todos los métodos reciben idEps; no existen búsquedas sin ese filtro. */
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByIdEpsAndDocumento(Long idEps, String documento);

    Optional<Paciente> findByIdEpsAndIdPaciente(Long idEps, Long idPaciente);

    /** Expediente del usuario con rol PACIENTE. */
    Optional<Paciente> findByIdEpsAndIdUsuario(Long idEps, Long idUsuario);

    boolean existsByIdEpsAndDocumento(Long idEps, String documento);
}
