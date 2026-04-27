package com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "empresa_cliente")
public class EmpresaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa_cliente")
    private Integer idEmpresaCliente;

    @Column(name = "codigo_empresa_cliente", nullable = false, unique = true, length = 45)
    private String codigoEmpresaCliente;

    @Column(name = "rut_empresa_cliente", nullable = false, unique = true, length = 45)
    private String rutEmpresaCliente;

    @Column(name = "razon_social_empresa_cliente", nullable = false, unique = true, length = 45)
    private String nombreRazonSocialEmpresaCliente;

    @Column(name = "nombre_empresa_cliente", nullable = false, unique = true, length = 45)
    private String nombreEmpresaCliente;

    @Column(name = "url_empresa_cliente", nullable = false, unique = true, length = 45)
    private String urlEmpresaCliente;


    //@OneToMany(mappedBy = "unidad", fetch = FetchType.LAZY)
    //private List<OrdenCompra> ordenesCompra;
}
