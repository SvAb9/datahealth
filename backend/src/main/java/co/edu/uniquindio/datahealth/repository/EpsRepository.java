package co.edu.uniquindio.datahealth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.Eps;

public interface EpsRepository extends JpaRepository<Eps, Long> {

    /** Para el selector del autorregistro. */
    List<Eps> findByActivaTrueOrderByNombreAsc();
}