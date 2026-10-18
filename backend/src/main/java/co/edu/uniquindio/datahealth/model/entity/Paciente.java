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

@Entity
@Table(name = "paciente")
@Getter @Setter @NoArgsConstructor
public class Paciente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long idPaciente;

    @Column(name = "id_eps", nullable = false, updatable = false)
    private Long idEps;

    /** Usuario con rol PACIENTE asociado a este expediente (puede ser nulo). */
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false)
    private String documento;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer edad;

    @Column(nullable = false)
    private String telefono;

    private String direccion;

    @Column(nullable = false)
    private Boolean activo = true;

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