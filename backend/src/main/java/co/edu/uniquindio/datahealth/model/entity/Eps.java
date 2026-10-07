package co.edu.uniquindio.datahealth.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "eps")
@Getter @Setter @NoArgsConstructor
public class Eps {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_eps")
    private Long idEps;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private Boolean activa = true;
}

