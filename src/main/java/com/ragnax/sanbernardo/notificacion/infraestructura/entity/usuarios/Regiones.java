package com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "regiones")
public class Regiones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_region")
    private Integer idRegion;

    @Column(name = "codigo_region", nullable = false, unique = true, length = 45)
    private String codigoRegion;

    @Column(name = "nombre_region", nullable = false, unique = true, length = 45)
    private String nombreRegion;

    //@OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    //private List<Comunas> comunas;
}
