package com.ragnax.sanbernardo.notificacion.infraestructura.repository;

import com.ragnax.sanbernardo.notificacion.infraestructura.entity.EjecutarProcesoCarta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface EjecutarProcesoCartaRepository extends JpaRepository<EjecutarProcesoCarta, Long> {

    //, String tipoCarta, String unidad
    List<EjecutarProcesoCarta> findByEjecutado(Boolean ejecutado);

}
