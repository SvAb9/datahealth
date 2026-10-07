package co.edu.uniquindio.datahealth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.Eps;

public interface EpsRepository extends JpaRepository<Eps, Long> {
}