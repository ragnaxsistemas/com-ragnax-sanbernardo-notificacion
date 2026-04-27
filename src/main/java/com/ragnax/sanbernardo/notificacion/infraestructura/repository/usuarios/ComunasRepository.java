package com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios;

import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.Comunas;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.Regiones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComunasRepository extends JpaRepository<Comunas, Integer> {

    Optional<Comunas> findByCodigoComuna(String codigoComuna);
    List<Comunas> findByRegion(Regiones region);
}
