package com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "unidad")
public class    Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad")
    private Integer idUnidad;

    @Column(name = "codigo_unidad", nullable = false, unique = true, length = 45)
    private String codigoUnidad;

    @Column(name = "nombre_unidad", nullable = false, unique = true, length = 45)
    private String nombreUnidad;

    @Column(name = "show_nombre_unidad", nullable = false,  length = 45)
    private String showNombreUnidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_empresa_cliente", nullable = false,
            foreignKey = @ForeignKey(name = "fk_unidad_empresa_cliente"))
    private EmpresaCliente empresaCliente;

}
