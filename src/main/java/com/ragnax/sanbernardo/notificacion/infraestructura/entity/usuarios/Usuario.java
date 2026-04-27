package com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 *  implementation class for @Entity: Usuario
 * en la base de Datos representa el registro de las personas asociadas al servicio.
 */
@Entity
@Table (name="usuarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario implements Serializable{

	private static final long serialVersionUID = 2536135838758090437L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id_usuario")
	private Long id;

	@Column(name="username", length = 50)
	private String username;

	@Column(name="password", length = 60)
	private String password;

    @OrderBy
    @Column(name="nombre_member")
    private String nombreMember;/****Nombre de la Persona****/

    @Column(name="apellido_paterno_member")
    private String apellidoPaternoMember; /****Apellido de la Persona****/

    @Column(name="rut")
    private String rut; /****Apellido de la Persona****/

    @OrderBy
    @Column(name="email_perfil")
    private String emailPerfil; /****mail usuario ****/

    @Column(name="telefono_contacto_member")
    private String telefonoContactoMember;

    @ManyToOne(targetEntity = Unidad.class, fetch=FetchType.LAZY)
    @JoinColumn(name="fk_id_unidad")
    private Unidad idUnidad;

	@ManyToOne(targetEntity = Role.class, fetch=FetchType.LAZY)
	@JoinColumn(name="fk_id_role")
	private Role idRole;

    @Column(name="active")
    private Boolean active;
}
