package com.ragnax.sanbernardo.notificacion.infraestructura.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "carpeta_habilitada")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarpetaHabilitada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carpeta_habilitada")
    private Long id;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "tipo_unidad", nullable = false, length = 50)
    private String tipoUnidad;

    @CreationTimestamp
    @Column(name = "fecha_habilitacion", updatable = false)
    private LocalDateTime fechaHabilitacion;

    @Column(name = "ubicacion_carpeta_habilitada", length = 50)
    private String ubicacion;

    @Column(name = "usuario_habilitante", length = 60)
    private String usuarioHabilitante;

    @Column(name = "habilitada", length = 60)
    private String habilitada;
}
