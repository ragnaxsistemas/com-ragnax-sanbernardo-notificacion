package com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios;

import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.Regiones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionesRepository extends JpaRepository<Regiones, Integer> {

    Optional<Regiones> findByCodigoRegion(String codigoRegion);
}
