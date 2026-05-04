package com.ragnax.sanbernardo.notificacion.infraestructura.repository;

import com.ragnax.sanbernardo.notificacion.infraestructura.entity.CdnSeguimiento;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.ProcesoCarta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CdnSeguimientoRepository extends JpaRepository<CdnSeguimiento, Integer> {

}
