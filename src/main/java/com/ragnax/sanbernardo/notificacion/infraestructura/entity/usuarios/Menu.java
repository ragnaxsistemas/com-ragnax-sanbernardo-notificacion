package com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {


   // @Column(name = "id_menu")
   // private Integer idMenu;ø
   @Id
    @Column(name = "cod_menu")
    private String codMenu;

    @Column(name = "icono")
    private String icono;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "url")
    private String url;

    @Column(name = "active")
    private Boolean estadoMenu;

    @Column(name = "orden")
    private Integer orden;

    //@OneToMany(mappedBy = "menu", fetch = FetchType.LAZY)
    //private List<MenuRol> menuRoles;
}
