package co.edu.uniquindio.datahealth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.datahealth.model.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Excepción a la regla "todo método recibe idEps" (ADR-03): en el login aún no se conoce la EPS,
     * y el correo es único en todo el sistema.
     */
    Optional<Usuario> findByEmailIgnoreCase(String email);
}