package co.edu.uniquindio.datahealth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.ResultadoExamen;

public interface ResultadoExamenRepository extends JpaRepository<ResultadoExamen, Long> {

    List<ResultadoExamen> findByIdEpsAndIdCita(Long idEps, Long idCita);
}
