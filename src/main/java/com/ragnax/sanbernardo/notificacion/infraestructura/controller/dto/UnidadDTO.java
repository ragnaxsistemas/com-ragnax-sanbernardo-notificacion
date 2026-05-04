package com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UnidadDTO implements Serializable {
    private static final long serialVersionUID = -1098427707835311622L;

    private String codigoUnidad;

    private String nombreUnidad;

    private String showNombreUnidad;

    private String codEmpresa;
}
