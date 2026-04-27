package com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios;

import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.MenuRol;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRolRepository extends JpaRepository<MenuRol, Integer> {
    List<MenuRol> findByRole(Role role);
}
