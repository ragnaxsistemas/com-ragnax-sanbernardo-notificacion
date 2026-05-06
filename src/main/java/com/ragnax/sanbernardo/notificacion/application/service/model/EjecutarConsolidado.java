package com.ragnax.sanbernardo.notificacion.application.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

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

}
