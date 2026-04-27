package com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios;

import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.GiroSii;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiroSiiRepository extends JpaRepository<GiroSii, Integer> {

    Optional<GiroSii> findByCodigoGiroSii(String codigoGiroSii);
}
