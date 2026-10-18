package co.edu.uniquindio.datahealth.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.CorreccionCita;

public interface CorreccionCitaRepository extends JpaRepository<CorreccionCita, Long> {

    List<CorreccionCita> findByIdEpsAndIdCitaInOrderByFechaHora(Long idEps, Collection<Long> idsCitas);
}
