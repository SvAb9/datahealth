package co.edu.uniquindio.datahealth.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Examen que el paciente trae a la consulta (RF-12). */
@Entity
@Table(name = "examen_paciente")
@Getter @Setter @NoArgsConstructor
public class ExamenPaciente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_examen")
    private Long idExamen;

    @Column(name = "id_eps", nullable = false, updatable = false)
    private Long idEps;

    @Column(name = "id_cita", nullable = false, updatable = false)
    private Long idCita;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "fecha_examen")
    private LocalDate fechaExamen;

    @Column(name = "creado_por")
    private Long creadoPor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() { fechaCreacion = LocalDateTime.now(); }
}