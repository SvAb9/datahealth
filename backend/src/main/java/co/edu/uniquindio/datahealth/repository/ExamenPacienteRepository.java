package co.edu.uniquindio.datahealth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.ExamenPaciente;

public interface ExamenPacienteRepository extends JpaRepository<ExamenPaciente, Long> {

    List<ExamenPaciente> findByIdEpsAndIdCita(Long idEps, Long idCita);
}
