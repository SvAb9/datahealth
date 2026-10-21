package co.edu.uniquindio.datahealth.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.RegistroAuditoria;

public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

    /** ADR-03: la auditoría que ve cada administrador es solo la de su EPS. */
    Page<RegistroAuditoria> findByIdEps(Long idEps, Pageable pageable);

    Page<RegistroAuditoria> findByIdEpsAndAccion(Long idEps, String accion, Pageable pageable);
}