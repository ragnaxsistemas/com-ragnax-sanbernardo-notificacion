package com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "giro_sii")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiroSii {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_giro_sii")
    private Integer idGiroSii;

    @Column(name = "codigo_giro_sii", nullable = false, unique = true, length = 45)
    private String codigoGiroSii;

    @Column(name = "nombre_giro_sii", nullable = false, unique = true, length = 45)
    private String nombreGiroSii;

    @Column(name = "afecto_iva_sii", nullable = false, unique = true, length = 45)
    private String afectoIvaSii;

    @Column(name = "categoria_tributaria_sii", nullable = false, unique = true, length = 45)
    private String categoriaTributariaSii;

    @Column(name = "disponible_internet_sii", nullable = false, unique = true, length = 45)
    private String disponibleInternetSii;

    @Column(name = "sub_rubro_sii", nullable = false, unique = true, length = 45)
    private String subRubroSii;

    @Column(name = "rubro_sii", nullable = false, unique = true, length = 45)
    private String rubroSii;

}
