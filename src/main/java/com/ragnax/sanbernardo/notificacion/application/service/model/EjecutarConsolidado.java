package com.ragnax.sanbernardo.notificacion.application.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.DescargasImprenta;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.ItemValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EjecutarConsolidado implements Serializable {

    private String tipo;
    private String unidad;
    private String pathArchivoConsolidado; //Cobranza -  Norificacion
    private Boolean activarConsolidadoImprenta;
    private List<DescargasImprenta> descargasImprenta;

    public EjecutarConsolidado(String tipo, String unidad, String pathArchivoConsolidado, Boolean activarConsolidadoImprenta) {
        this.tipo = tipo;
        this.unidad = unidad;
        this.pathArchivoConsolidado = pathArchivoConsolidado;
        this.activarConsolidadoImprenta = activarConsolidadoImprenta;
    }
}
