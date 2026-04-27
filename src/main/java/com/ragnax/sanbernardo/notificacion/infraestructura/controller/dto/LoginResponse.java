package com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto;

import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.EmpresaCliente;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.Role;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.Unidad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String username;

    private String nombreMember;/****Nombre de la Persona****/

    private String apellidoPaternoMember; /****Apellido de la Persona****/

    private String apellidoMaternoMember; /****Apellido de la Persona****/

    private String telefonoContactoMember;

    private String emailPerfil; /****mail usuario ****/

    private Unidad unidad; /****mail usuario ****/

    private EmpresaCliente empresa;

    private Role role;

    private List<ItemValue> items;

    private String token;
}
