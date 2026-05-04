package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class ExcelCobranzaImpresion extends ExcelCobranzaMerge implements Serializable {

    private String procCorrelativoImpresion;
    private String nombreArchivo;
    private String codigoCd;
    private String correlativoHistorico;
    private String contFolioProceso;
    private String contFolioTipo;
    private String cantidadPatente;
    //private String html;
    private byte[] pdf;
    // private String bloqueActualExcelCobranza;

}

