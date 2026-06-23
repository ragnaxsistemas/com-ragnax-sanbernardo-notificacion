package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class ExcelNotificacionImpresion extends ExcelNotificacionMerge implements Serializable {

    private String correlativoHistorico;
    private String procCorrelativoHistorico;
    private String correlativoImpresion;
    private String contFolioTipo;

}

