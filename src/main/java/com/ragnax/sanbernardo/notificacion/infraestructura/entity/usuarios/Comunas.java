package com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "comunas")
public class Comunas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comuna")
    private Integer idComuna;

    @Column(name = "codigo_comuna", nullable = false, length = 45)
    private String codigoComuna;

    @Column(name = "nombre_comuna", nullable = false, length = 45)
    private String nombreComuna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_region", nullable = false,
                foreignKey = @ForeignKey(name = "fk_comuna_region"))
    private Regiones region;

}
