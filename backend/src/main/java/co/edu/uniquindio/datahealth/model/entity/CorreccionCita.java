package co.edu.uniquindio.datahealth.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Corrección posterior al cierre: una fila nueva; la cita original nunca se sobrescribe (ADR-05). */
@Entity
@Table(name = "correccion_cita")
@Getter @Setter @NoArgsConstructor
public class CorreccionCita {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_correccion")
    private Long idCorreccion;

    @Column(name = "id_eps", nullable = false, updatable = false)
    private Long idEps;

    @Column(name = "id_cita", nullable = false, updatable = false)
    private Long idCita;

    @Column(nullable = false)
    private String motivo;

    private String detalle;

    @Column(name = "id_usuario", nullable = false, updatable = false)
    private Long idUsuario;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
}