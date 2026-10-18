package co.edu.uniquindio.datahealth.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.OrdenMedica;

public interface OrdenRepository extends JpaRepository<OrdenMedica, Long> {

    List<OrdenMedica> findByIdEpsAndIdCitaInOrderByIdOrden(Long idEps, Collection<Long> idsCitas);
}
