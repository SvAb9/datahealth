package co.edu.uniquindio.datahealth.model.entity;

import java.time.LocalDateTime;

import co.edu.uniquindio.datahealth.model.enums.EstadoCita;
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

/** Cita/atención: una consulta realizada al paciente (ADR-05). Cerrada = inmodificable. */
@Entity
@Table(name = "cita")
@Getter @Setter @NoArgsConstructor
public class Cita {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita;

    @Column(name = "id_eps", nullable = false, updatable = false)
    private Long idEps;

    @Column(name = "id_paciente", nullable = false, updatable = false)
    private Long idPaciente;

    @Column(name = "id_medico", nullable = false, updatable = false)
    private Long idMedico;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.ABIERTA;

    private String diagnostico;

    private String observaciones;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "cerrado_por")
    private Long cerradoPor;

    @Column(name = "creado_por")
    private Long creadoPor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "modificado_por")
    private Long modificadoPor;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    void antesDeGuardar() { fechaCreacion = LocalDateTime.now(); }
}