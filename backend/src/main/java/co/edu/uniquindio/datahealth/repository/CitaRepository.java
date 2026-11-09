package co.edu.uniquindio.datahealth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.Cita;

import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByIdEpsAndIdPacienteOrderByFechaDesc(Long idEps, Long idPaciente);

    // CitaRepository
    Optional<Cita> findByIdEpsAndIdCita(Long idEps, Long idCita);   // import java.util.Optional
}
