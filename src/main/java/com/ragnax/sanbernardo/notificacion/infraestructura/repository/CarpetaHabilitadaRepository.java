package com.ragnax.sanbernardo.notificacion.infraestructura.repository;

import com.ragnax.sanbernardo.notificacion.infraestructura.entity.CarpetaHabilitada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarpetaHabilitadaRepository extends JpaRepository<CarpetaHabilitada, Integer> {

    // Ejemplo de búsqueda por tipo de unidad para el perfil que consulta
    List<CarpetaHabilitada> findByTipoUnidad(String tipoUnidad);

    // Buscar por tipo específico
    List<CarpetaHabilitada> findByTipo(String tipo);

}
