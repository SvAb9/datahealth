package co.edu.uniquindio.datahealth.model.entity;

import java.time.LocalDateTime;

import co.edu.uniquindio.datahealth.model.enums.TipoOrden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Orden médica, de medicamento o de laboratorio emitida durante una cita. */
@Entity
@Table(name = "orden")
@Getter @Setter @NoArgsConstructor
public class OrdenMedica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;

    @Column(name = "id_eps", nullable = false, updatable = false)
    private Long idEps;

    @Column(name = "id_cita", nullable = false, updatable = false)
    private Long idCita;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOrden tipo;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "creado_por")
    private Long creadoPor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() { fechaCreacion = LocalDateTime.now(); }
}