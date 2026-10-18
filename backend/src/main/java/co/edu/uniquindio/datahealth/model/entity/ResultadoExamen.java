package co.edu.uniquindio.datahealth.model.entity;

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

/** Resultado de un examen; puede llegar después de cerrada la cita (ADR-05). */
@Entity
@Table(name = "resultado_examen")
@Getter @Setter @NoArgsConstructor
public class ResultadoExamen {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultado")
    private Long idResultado;

    @Column(name = "id_eps", nullable = false, updatable = false)
    private Long idEps;

    @Column(name = "id_orden", nullable = false, updatable = false)
    private Long idOrden;

    @Column(name = "id_cita", nullable = false, updatable = false)
    private Long idCita;

    @Column(nullable = false)
    private String resultado;

    @Column(name = "fecha_resultado", nullable = false)
    private LocalDateTime fechaResultado;

    @Column(name = "creado_por")
    private Long creadoPor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() { fechaCreacion = LocalDateTime.now(); }
}