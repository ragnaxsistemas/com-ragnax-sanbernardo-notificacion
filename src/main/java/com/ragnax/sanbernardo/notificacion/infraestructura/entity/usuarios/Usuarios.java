package com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuarios")
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", length = 60)
    private String password;

    @Column(name = "nombre_member")
    private String nombreMember;

    @Column(name = "apellido_paterno_member")
    private String apellidoPaternoMember;

    @Column(name = "rut", length = 15)
    private String rut;

    @Column(name = "email_perfil")
    private String emailPerfil;

    @Column(name = "telefono_contacto_member")
    private String telefonoContactoMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_unidad",
                foreignKey = @ForeignKey(name = "fk_usuario_unidad"))
    private Unidad idUnidad;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_id_role",
                foreignKey = @ForeignKey(name = "fk_usuario_role"))
    private Role idRole;

    @Column(name = "active")
    private Boolean active;

}
